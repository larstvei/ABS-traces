(ns visualize-traces-clj.core
  (:require [visualize-traces-clj.utils :as utils]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [clojure.string :as s]
            [clojure.set :refer [difference union]]
            [dommy.core :refer [listen! unlisten! parent style] :refer-macros [sel1]]))

(def json-str "[ { \"cog_id\": [ 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"main\" }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_setOtherClient\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_setOtherClient\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_setOtherClient\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_setOtherClient\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_insert\", \"task_id\": 2 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_insert\", \"task_id\": 3 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_printbuffer\", \"task_id\": 6 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_printbuffer\", \"task_id\": 6 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_printbuffer\", \"task_id\": 7 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_printbuffer\", \"task_id\": 7 } ] }, { \"cog_id\": [ 0, 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_setOtherClient\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_setOtherClient\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_insert\", \"task_id\": 2 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_insert\", \"task_id\": 2 }, { \"caller_id\": [ 0, 1 ], \"event_type\": \"schedule\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0, 1 ], \"event_type\": \"completed\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_printbuffer\", \"task_id\": 6 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_printbuffer\", \"task_id\": 6 } ] }, { \"cog_id\": [ 0, 1 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_setOtherClient\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_setOtherClient\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_insert\", \"task_id\": 3 }, { \"caller_id\": [ 0, 1 ], \"event_type\": \"invocation\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_insert\", \"task_id\": 3 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_printbuffer\", \"task_id\": 7 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_printbuffer\", \"task_id\": 7 } ] }, { \"cog_id\": [ 1 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" } ] } ]")
;; (def json-str "[ { \"cog_id\": [ 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"main\" }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_sort\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_sort\", \"task_id\": 0 } ] }, { \"cog_id\": [ 0, 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_sort\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"future_read\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"future_read\", \"method\": \"m_sort\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_sort\", \"task_id\": 0 } ] }, { \"cog_id\": [ 0, 0, 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_sortInternal\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sortInternal\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_sortInternal\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"future_read\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"future_read\", \"method\": \"m_sortInternal\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_sort\", \"task_id\": 1 } ] }, { \"cog_id\": [ 1 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" } ] } ]")

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

(def event-types [:schedule :invocation :completed :future-read])

(def event-type->color
  (let [n (quot 256 (count event-types))]
    (->> (count event-types) range
         (map #(vector (- 255 (* % n)) 255 255))
         (zipmap event-types))))


(defn color-map [trace]
  (let [task (fn [e] (when (= (:event-type e) :invocation) (:task-id e)))
        tasks (mapcat (fn [[cog schedule]]
                        (->> (keep task schedule)
                             (map (partial vector cog)))) trace)
        n (count tasks)
        c (/ 256 n)
        ;; Try to keep similar colors far apart from each other.
        indexes (concat (range 0 n 3) (range 1 n 3) (range 2 n 3))
        colors (map #(vector % (- 255 (* c %2))) tasks indexes)]
    (into {} colors)))

(defn json->trace [s]
  (->> (clojure.walk/prewalk (fn [x] (if (string? x) (keyword x) x)) s)
       (reduce (fn [res cog] (assoc res (:cog-id cog) (:cog-schedule cog))) {})))

(defn json-str->trace [s]
  (->> (s/replace s "_" "-") (.parse js/JSON) (js->clj) json->trace))

(defn blocked-events [trace]
  (-> (fn [[cog schedule]]
        (keep-indexed
         (fn [i event]
           (when (or (= (:event-type event) :schedule)
                     (= (:event-type event) :future-read))
             [cog i])) schedule))
      (mapcat trace) set))

(defn setup [trace]
  (q/frame-rate 1)
  (q/color-mode :hsb)
  (q/text-align :center)
  {:trace trace
   ;; Missing object creation events, so assume that all cogs spawn
   ;; at beginning of simulation (which is not always true).
   :blocked (difference (blocked-events trace)
                        (set (map (fn [cog] [cog 0]) (keys trace))))
   :pending (-> (fn [[cog schedule]]
                  (map-indexed (fn [i s] [cog i]) schedule))
                (mapcat trace) set)
   :history nil
   :cogs (count (keys trace))
   :event->color-map (color-map trace)
   :speed 1}
  )

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

(defn one-per-cog [event-keys]
  (let [cogs (group-by first event-keys)]
    (set (map (comp first (partial sort-by second)) (vals cogs)))))

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

(defn history->trace [history trace]
  (-> (fn [res event-keys]
        (-> (fn [res2 [cog i]]
              (let [e (get-in trace [cog i])]
                (update res2 cog (fnil conj []) e)))
            (reduce res event-keys)))
      (reduce {} history)))

(defn update-state [state]
  (if (and (not (:paused state)) (not-empty (:pending state)))
    (let [event-keys (->> state :pending one-per-cog
                          (remove (:blocked state)) set)
          history (conj (:history state) event-keys)
          enabled (mapcat #(enabled-by % (:trace state)) event-keys)]
      (-> state
          (assoc :history history)
          (update :blocked difference enabled)
          (update :pending difference event-keys)))
    state
    ;;(setup)
    ))

(defn draw-grid [n m wd hd]
  (q/no-stroke)
  (dotimes [h n]
    (q/fill (if (even? h) 245 250))
    (let [x2 (+ (* m wd) 40)]
      (q/rect (- wd 20) (* (+ h 0.5) hd) x2 hd))))

(defn display-cog-names [cogs wd hd]
  (doseq [cog cogs]
    (q/fill 0)
    (q/text cog (* wd (inc (.indexOf cogs cog))) (/ hd 2.5))))

(defn draw-message [event trace cogs history i j wd hd]
  (let [[event-key2] (enabled-by-invoc event trace)
        k (.indexOf cogs (first event-key2))
        xs (drop-while #(not (% event-key2)) history)]
    (when (not-empty xs)
      (let [l (- (count history) (count xs))
            x1 (* wd (inc j)) y1 (* hd (inc i))
            x2 (* wd (inc k)) y2 (* hd (inc l))]
        (q/fill 0)
        (q/stroke 0)
        (utils/dotted-arrow x1 y1 x2 y2)
        (utils/label-line (subs (str (:method event)) 3) x1 y1 x2 y2)))))

(defn draw-state [state]
  (q/frame-rate (:speed state))
  (q/text-font (q/create-font "monospace" 13))
  (q/background 255)
  (q/stroke-weight 1)
  (q/text-align :center)
  (let [history (reverse (:history state))
        wd (/ (q/width) (inc (:cogs state)))
        hd (min 50 (/ (q/height) (inc (count history))))
        cogs (->> (:trace state) keys sort to-array)]
    (draw-grid (count history) (dec (count cogs)) wd hd)
    (display-cog-names cogs wd hd)
    (doseq [[i event-keys]
            (map-indexed vector history)]
      (doseq [event-key event-keys]
        (let [event (get-in (:trace state) event-key)
              j (.indexOf cogs (first event-key))
              task [(or (:caller-id event) (first event-key)) (:task-id event)]
              hue ((:event->color-map state) task)]
          (when (= (:event-type event) :invocation)
            (draw-message event (:trace state) cogs history i j wd hd))

          (q/fill 255)
          (q/stroke-weight 2)
          (if hue (q/stroke hue 255 255) (q/stroke 0))
          (q/ellipse (* wd (inc j)) (* hd (inc i)) 15 15)
          (q/stroke 255)
          (apply q/fill (event-type->color (:event-type event)))
          (q/ellipse (* wd (inc j)) (* hd (inc i)) 10 10)
          (q/stroke-weight 1))))))

(defn sketch-size []
  (let [container (parent (sel1 (keyword "#visualize-traces-clj")))
        s (style container)]
    [(js/parseInt (.-width s)) (js/parseInt (.-height s))]))

(defn resize []
  (q/with-sketch (q/get-sketch-by-id "visualize-traces-clj")
    (apply q/resize-sketch (sketch-size))))

(unlisten! js/window :resize resize)
(listen! js/window :resize resize)

(defn key-handler [state event]
  (case (name (q/key-as-keyword))
    " "    (update state :paused not)

    "up"   (update state :speed inc)

    "down" (update state :speed dec)

    state))

(defn ^:export run-sketch []
  (q/defsketch visualize-traces-clj
    :host "visualize-traces-clj"
    :size (sketch-size)
    :setup (partial setup example-trace)
    :update update-state
    :key-pressed key-handler
    :draw draw-state
    :middleware [m/fun-mode]))
