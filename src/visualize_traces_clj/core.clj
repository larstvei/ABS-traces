(ns visualize-traces-clj.core
  (:require [visualize-traces-clj.utils :as utils]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [clojure.set :refer [difference union]]))

(def example-trace
  {[0]   [{:event-type :schedule, :task-id :main}
          {:caller-id [0], :event-type :invocation, :method :m-insert, :task-id 2}
          {:caller-id [0], :event-type :invocation, :method :m-insert, :task-id 3}]
   [0 0] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-insert, :task-id 2}
          {:caller-id [0 0], :event-type :invocation, :method :m-receive, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-insert, :task-id 2}
          {:caller-id [0 1], :event-type :schedule, :method :m-receive, :task-id 0}
          {:caller-id [0 1], :event-type :completed, :method :m-receive, :task-id 0}]
   [0 1] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-insert, :task-id 3}
          {:caller-id [0 1], :event-type :invocation, :method :m-receive, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-insert, :task-id 3}
          {:caller-id [0 0], :event-type :schedule, :method :m-receive, :task-id 0}
          {:caller-id [0 0], :event-type :completed, :method :m-receive, :task-id 0}]})

(def example-trace2
  {[0]   [{:event-type :schedule, :task-id :main}
          {:caller-id [0], :event-type :invocation, :method :m-p, :task-id 0}
          {:caller-id [0], :event-type :invocation, :method :m-q, :task-id 1}
          {:caller-id [0], :event-type :invocation, :method :m-h, :task-id 2}],
   [0 0] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-p, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-p, :task-id 0}
          {:caller-id [0 1], :event-type :schedule, :method :m-m, :task-id 0}
          {:caller-id [0 1], :event-type :completed, :method :m-m, :task-id 0}
          {:caller-id [0 2], :event-type :schedule, :method :m-t, :task-id 0}
          {:caller-id [0 2], :event-type :completed, :method :m-t, :task-id 0}],
   [0 1] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-q, :task-id 1}
          {:caller-id [0 1], :event-type :invocation, :method :m-m, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-q, :task-id 1}],
   [0 2] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-h, :task-id 2}
          {:caller-id [0 2], :event-type :invocation, :method :m-t, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-h, :task-id 2}]})

(def example-trace3
  {[0]   [{:event-type :schedule, :task-id :main}
          {:caller-id [0], :event-type :invocation, :method :m-work, :task-id 0}
          {:caller-id [0], :event-type :invocation, :method :m-abort, :task-id 1}],
   [0 0] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-work, :task-id 0}
          {:caller-id [0 0], :event-type :invocation, :method :m-work, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-work, :task-id 0}
          {:caller-id [0], :event-type :schedule, :method :m-abort, :task-id 1}
          {:caller-id [0], :event-type :completed, :method :m-abort, :task-id 1}
          {:caller-id [0 0], :event-type :schedule, :method :m-work, :task-id 0}
          {:caller-id [0 0], :event-type :completed, :method :m-work, :task-id 0}]})

(def example-trace3-wanted-result
  #{{[0]   [{:event-type :schedule, :task-id :main}
            {:caller-id [0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :invocation, :method :m-abort, :task-id 1}],
     [0 0] [{:event-type :schedule, :task-id :init}
            {:caller-id [0], :event-type :schedule, :method :m-work, :task-id 0}
            {:caller-id [0 0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :completed, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :schedule, :method :m-abort, :task-id 1}
            {:caller-id [0], :event-type :completed, :method :m-abort, :task-id 1}
            {:caller-id [0 0], :event-type :schedule, :method :m-work, :task-id 0}
            {:caller-id [0 0], :event-type :completed, :method :m-work, :task-id 0}]}

    {[0]   [{:event-type :schedule, :task-id :main}
            {:caller-id [0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :invocation, :method :m-abort, :task-id 1}],
     [0 0] [{:event-type :schedule, :task-id :init}
            {:caller-id [0], :event-type :schedule, :method :m-abort, :task-id 1}]}

    {[0]   [{:event-type :schedule, :task-id :main}
            {:caller-id [0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :invocation, :method :m-abort, :task-id 1}],
     [0 0] [{:event-type :schedule, :task-id :init}
            {:caller-id [0], :event-type :schedule, :method :m-work, :task-id 0}
            {:caller-id [0 0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :completed, :method :m-work, :task-id 0}
            {:caller-id [0 0], :event-type :schedule, :method :m-work, :task-id 0}]}})

(defn json->trace [s]
  (->> (clojure.walk/prewalk (fn [x] (if (string? x) (keyword x) x)) s)
       (reduce (fn [res cog] (assoc res (:cog-id cog) (:cog-schedule cog))) {})))

(defn json-str->trace [s]
  (->> (s/replace s "_" "-") json/read-json json->trace))

(defn event-key->event [trace k]
  (get-in trace k))

(defn blocked-events [trace]
  (-> (fn [[cog schedule]]
        (keep-indexed
         (fn [i event]
           (when (or (= (:event-type event) :schedule)
                     (= (:event-type event) :future-read))
             [cog i])) schedule))
      (mapcat trace) set))

(defn enables [pred trace]
  (->> trace
       (keep (fn [[cog schedule]]
               (->> schedule
                    (map-indexed vector)
                    (filter (comp pred second))
                    (map first)
                    (map (partial vector cog))
                    (not-empty))))
       (apply concat)))

(defn enabled-by-invoc [event trace]
  (enables (partial = (assoc event :event-type :schedule)) trace))

(defn enabled-by-completion [event trace]
  (enables (partial = (assoc event :event-type :future-read)) trace))

(defn enabled-by [event-key trace]
  (let [event (get-in trace event-key)]
    (case (:event-type event)
      :invocation (enabled-by-invoc event trace)
      :completed (enabled-by-completion event trace)
      #{})))

(defn event-key-type [trace event-key]
  (get-in trace (conj event-key :event-type)))

(defn event-key-method [trace event-key]
  (or (get-in trace (conj event-key :method))
      (get-in trace (conj event-key :task-id))))

(defn event-key-method-name [trace event-key]
  (let [method (event-key-method trace event-key)]
    (s/replace-first (name method) "m-" "")))

(defn event-key-task [trace event-key]
  (let [event (event-key->event trace event-key)]
    [(or (:caller-id event) (first event-key)) (:task-id event)]))

(defn schedule-bulk [trace schedule]
  (-> (fn [k] (-> (event-key-type trace k) (not= :schedule)))
      (take-while schedule)))

(defn one-per-cog [event-keys]
  (let [cogs (group-by first event-keys)]
    (set (map (comp first (partial sort-by second)) (vals cogs)))))

(defn one-schedule-run-per-cog [trace event-keys]
  (let [cogs (group-by first event-keys)]
    (reduce (fn [res [cog schedule]]
              (let [[x & xs] (sort-by second schedule)
                    non-schedule-events (schedule-bulk trace xs)]
                (conj res (conj non-schedule-events x))))
            #{}
            cogs)))

(defn schedule-runs [trace event-keys]
  (when-not (empty? event-keys)
    (let [[x & xs] event-keys
          [run ys] (split-with (comp (partial not= :schedule)
                                     (partial event-key-type trace))
                               xs)]
      (cons (cons x run) (schedule-runs trace ys)))))

(defn unblock-init [trace]
  (let [inits (set (map (fn [cog] [cog 0]) (keys trace)))]
    (difference (blocked-events trace) inits)))

(defn cog-local-trace->event-keys [cog schedule]
  (for [i (range (count schedule))]
    [cog i]))

(defn trace->event-keys [trace]
  (-> (fn [res [cog schedule]]
        (into res (cog-local-trace->event-keys cog schedule)))
      (reduce #{} trace)))

#_(defn trace->queue-tree
    ([trace] (let [blocked (unblock-init trace)
                   pending (trace->event-keys trace)]
               (trace->queue-tree trace blocked pending)))
    ([trace blocked pending qtree]
     (when-not (empty? pending)
       (let [candidates (one-schedule-run-per-cog pending)
             entries (difference candidates blocked)
             enabled (set (mapcat #(enabled-by % trace) entries))]
         {:queues entries
          :actions (filter ? entries)
          :children (map (fn [?] (trace->queue-tree )) actions)}))))

(defn trace->process-paths
  ([trace] (let [blocked (unblock-init trace)
                 pending (trace->event-keys trace)]
             (trace->process-paths trace blocked pending)))
  ([trace blocked pending]
   (if (empty? pending)
     '(())
     (let [candidates (schedule-runs trace (sort pending))
           candidates (or (not-empty (filter (comp zero? second first) candidates))
                          candidates)]
       (apply concat
              (for [x (remove (comp blocked first) candidates)
                    :let [[c1 i1] (first x)
                          enabled (set (mapcat #(enabled-by % trace) x))]]
                (if-not (empty? (filter (fn [[c2 i2]] (and (= c1 c2) (> i1 i2))) pending))
                  (list (list (list [c1 i1])))
                  (map (partial cons x)
                       (trace->process-paths
                        trace
                        (difference blocked enabled)
                        (difference pending (set x)))))))))))

(defn trace->process-queues
  ([trace] (let [blocked (unblock-init trace)
                 pending (trace->event-keys trace)]
             (trace->process-queues trace blocked pending nil)))
  ([trace blocked pending process-queues]
   (if (empty? pending)
     (reverse process-queues)
     (let [candidates (mapcat identity (one-schedule-run-per-cog trace pending))
           entries (difference candidates blocked)
           enabled (set (mapcat #(enabled-by % trace) entries))]
       (recur trace
              (difference blocked enabled)
              (difference pending entries)
              (conj process-queues
                    (filter (fn [k] (= (event-key-type trace k) :schedule))
                            (difference pending blocked))))))))

(defn trace->schedule-history
  ([trace] (let [blocked (unblock-init trace)
                 pending (trace->event-keys trace)]
             (trace->schedule-history trace blocked pending nil)))
  ([trace blocked pending history]
   (if (empty? pending)
     (reverse history)
     (let [candidates (mapcat identity (one-schedule-run-per-cog trace pending))
           entries (difference candidates blocked)
           enabled (set (mapcat #(enabled-by % trace) entries))]
       (recur trace
              (difference blocked enabled)
              (difference pending entries)
              (conj history entries))))))

(defn trace->history
  ([trace] (let [blocked (unblock-init trace)
                 pending (trace->event-keys trace)]
             (trace->history trace blocked pending nil)))
  ([trace blocked pending history]
   (if (empty? pending)
     (reverse history)
     (let [candidates (one-per-cog pending)
           entries (difference candidates blocked)
           enabled (set (mapcat #(enabled-by % trace) entries))]
       (recur trace
              (difference blocked enabled)
              (difference pending entries)
              (conj history entries))))))

(defn history->trace [trace history]
  (-> (fn [res event-keys]
        (-> (fn [res2 [cog i]]
              (let [e (get-in trace [cog i])]
                (update res2 cog (fnil conj []) e)))
            (reduce res event-keys)))
      (reduce {} history)))

(defn setup [trace]
  (q/color-mode :hsb)
  (q/text-align :center :center)
  (q/text-font (q/create-font "monospace" 16))
  (q/stroke-weight 2)
  {:trace trace
   :cogs (keys trace)
   :history (trace->history trace)
   :speed 1
   :start 0
   :height 30})

(def event-color
  (memoize
   (let [color (atom 0)]
     (fn [event-key]
       (swap! color (comp #(mod % 256) (partial + (+ 128 32))))
       [@color 128 256]))))

(defn draw-grid [n m wd hd]
  (q/no-stroke)
  (let [width (- (q/width) (* 2 wd))]
    (dotimes [h m]
      (let [y1 (+ (/ hd 2) (* h hd))]
        (q/fill (if (even? h) 245 250))
        (q/rect wd y1 width hd)))))

(defn draw-cogs [cogs wd hd]
  (q/fill 0)
  (doseq [cog cogs]
    (let [x (* wd (inc (.indexOf cogs cog)))
          y (/ hd 3)]
      (q/text (str cog) x y))))

(defn draw-events [trace history cogs wd hd]
  (q/text-size 14)
  (doseq [[i events] (map-indexed vector history)]
    (let [y (* (inc i) hd)]
      (doseq [[cog id] events]
        (let [type (event-key-type trace [cog id])
              task (event-key-task trace [cog id])
              method (event-key-method-name trace [cog id])
              x (* wd (inc (.indexOf cogs cog)))]
          (apply q/fill (event-color task))
          (q/no-stroke)
          (q/ellipse x y 15 15)
          (q/fill 0)
          (q/text method x (- y (/ hd 4)))
          (q/text (name type) x (+ y (/ hd 4)))
          (q/stroke 0)
          (doseq [[cog2 id2] (enabled-by [cog id] trace)]
            (let [x2 (* wd (inc (.indexOf cogs cog2)))
                  j  (count (take-while (complement #(% [cog2 id2])) history))
                  y2 (* (inc j) hd)
                  method (event-key-method-name trace [cog id])]
              (utils/dotted-arrow x y x2 y2))))))))

(defn draw-state [state]
  (q/frame-rate (:speed state))
  (q/background 255)
  (let [history (take (:height state) (drop (:start state) (:history state)))
        n (count (:cogs state))
        m (count history)
        wd (/ (q/width) (inc n))
        hd (/ (q/height) (inc m))]
    (draw-grid n m (/ wd 2) hd)
    (draw-cogs (:cogs state) wd hd)
    (draw-events (:trace state) history (:cogs state) wd hd)))

(defn key-handler [state event]
  (case (name (q/key-as-keyword))
    " "    (update state :paused not)

    "up"   (update state :speed inc)

    "down" (update state :speed dec)

    state))

(defn ^:export run-sketch []
  (q/defsketch visualize-traces-clj
    :host "visualize-traces-clj"
    :size [800 1000]
    :setup (partial setup example-trace)
    :key-pressed key-handler
    :draw draw-state
    :settings (partial q/pixel-density 2)
    :features [:resizable]
    :middleware [m/fun-mode m/pause-on-error]))
