(ns visualize-traces-clj.core
  (:require [visualize-traces-clj.utils :as utils]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [clojure.string :as s]
            [clojure.set :refer [difference union]]
            [dommy.core :refer [listen! unlisten! parent style] :refer-macros [sel1]]))

(def json-str "[ { \"cog_id\": [ 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"main\" }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_setOtherClient\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_setOtherClient\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_setOtherClient\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_setOtherClient\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_insert\", \"task_id\": 2 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_insert\", \"task_id\": 3 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_printbuffer\", \"task_id\": 6 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_printbuffer\", \"task_id\": 6 }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_printbuffer\", \"task_id\": 7 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_printbuffer\", \"task_id\": 7 } ] }, { \"cog_id\": [ 0, 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_setOtherClient\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_setOtherClient\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_insert\", \"task_id\": 2 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_insert\", \"task_id\": 2 }, { \"caller_id\": [ 0, 1 ], \"event_type\": \"schedule\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0, 1 ], \"event_type\": \"completed\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_done\", \"task_id\": 4 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_printbuffer\", \"task_id\": 6 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_printbuffer\", \"task_id\": 6 } ] }, { \"cog_id\": [ 0, 1 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_setOtherClient\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_setOtherClient\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_insert\", \"task_id\": 3 }, { \"caller_id\": [ 0, 1 ], \"event_type\": \"invocation\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_insert\", \"task_id\": 3 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_receive\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_done\", \"task_id\": 5 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_printbuffer\", \"task_id\": 7 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_printbuffer\", \"task_id\": 7 } ] }, { \"cog_id\": [ 1 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" } ] } ]")
;; (def json-str "[ { \"cog_id\": [ 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"main\" }, { \"caller_id\": [ 0 ], \"event_type\": \"invocation\", \"method\": \"m_sort\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"future_read\", \"method\": \"m_sort\", \"task_id\": 0 } ] }, { \"cog_id\": [ 0, 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_sort\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 0 }, { \"caller_id\": [ 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"future_read\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"future_read\", \"method\": \"m_sort\", \"task_id\": 1 }, { \"caller_id\": [ 0 ], \"event_type\": \"completed\", \"method\": \"m_sort\", \"task_id\": 0 } ] }, { \"cog_id\": [ 0, 0, 0 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"invocation\", \"method\": \"m_sortInternal\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sortInternal\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_sortInternal\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"schedule\", \"method\": \"m_sort\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"future_read\", \"method\": \"m_sortInternal\", \"task_id\": 0 }, { \"caller_id\": [ 0, 0, 0 ], \"event_type\": \"future_read\", \"method\": \"m_sortInternal\", \"task_id\": 1 }, { \"caller_id\": [ 0, 0 ], \"event_type\": \"completed\", \"method\": \"m_sort\", \"task_id\": 1 } ] }, { \"cog_id\": [ 1 ], \"cog_schedule\": [ { \"event_type\": \"schedule\", \"task_id\": \"init\" } ] } ]")

(def event-types [:schedule :invocation :completed :future-read])

(def event-type->color
  (->> (count event-types) range
       (map #(vector (/ 255 (inc %)) 255 255))
       (zipmap event-types)))

(defn json->state [s]
  (->> (s/replace s "_" "-") (.parse js/JSON) (js->clj)
       (clojure.walk/prewalk (fn [x] (if (string? x) (keyword x) x)))
       (reduce (fn [res cog] (assoc res (:cog-id cog) (:cog-schedule cog))) {})))

(defn setup []
  (q/frame-rate 1)
  (q/color-mode :hsb)
  (q/text-align :center)
  (let [data (json->state json-str)]
    {:data data
     ;; Missing object creation events, so assume that all cogs spawn
     ;; at beginning of simulation (which is not always true).
     :enabled (set (map (fn [cog] [cog 0]) (keys data)))
     :history nil
     :cogs (count (keys data))}))

(defn enables [pred data]
  (-> (fn [[cog schedule]]
        (let [sched-event (->> schedule
                               (map-indexed vector)
                               (filter (comp pred second))
                               first)]
          (when sched-event [cog (first sched-event)])))
      (keep data)))

(defn enabled-by-sequential [event-key data]
  (let [new-key (update event-key 1 inc)
        event (get-in data new-key)]
    (case (:event-type event)
      :schedule #{}
      :future-read #{}
      (if event #{new-key} #{}))))

(defn enabled-by-invoc [event data]
  (enables (partial = (assoc event :event-type :schedule)) data))

(defn enabled-by-completion [event data]
  (enables (partial = (assoc event :event-type :future-read)) data))

(defn enabled-by [event-key data]
  (let [event (get-in data event-key)]
    (into (enabled-by-sequential event-key data)
          (case (:event-type event)
            :invocation (enabled-by-invoc event data)
            :completed (enabled-by-completion event data)
            nil))))

(defn remove-completed [enabled history]
  (let [in-history? (apply union history)]
    (-> (fn [[cog schedule :as event-key]]
          (in-history? event-key))
        (remove enabled) set)))

(defn one-per-cog [event-keys]
  (let [cogs (group-by first event-keys)]
    (set (map first (vals cogs)))))

(defn update-state [state]
  (if (not-empty (:enabled state))
    (let [event-keys (->> state :enabled one-per-cog
                          (filter (:enabled state)) set)
          history (conj (:history state) event-keys)]
      (-> state
          (assoc :history history)
          (update :enabled (partial reduce into)
                  (map #(enabled-by % (:data state)) event-keys))
          (update :enabled remove-completed history)))
    state))

(defn draw-state [state]
  (q/text-font (q/create-font "monospace" 13))
  (q/background 255)
  (q/stroke-weight 1)
  (let [history (reverse (:history state))
        wd (/ (q/width) (inc (:cogs state)))
        hd (min 50 (/ (q/height) (inc (count history))))
        cogs (->> (:data state) keys sort to-array)]
    (dotimes [h (count history)]
      (q/no-stroke)
      (q/fill (if (even? h) 245 250))
      (let [x2 (+ (* (dec (count cogs)) wd) 40)]
        (q/rect (- wd 20) (* (+ h 0.5) hd) x2 hd)))
    (doseq [cog cogs]
      (q/fill 0)
      (q/text cog (* wd (inc (.indexOf cogs cog))) 12))
    (doseq [[i event-keys]
            (map-indexed vector history)]
      (doseq [event-key event-keys]
        (let [event (get-in (:data state) event-key)
              j (.indexOf cogs (first event-key))]
          (when (= (:event-type event) :invocation)
            (let [[event-key2] (enabled-by-invoc event (:data state))
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
          (q/fill 255)
          (apply q/stroke (event-type->color (:event-type event)))
          (q/ellipse (* wd (inc j)) (* hd (inc i)) 10 10))))))


(defn sketch-size []
  (let [container (parent (sel1 (keyword "#visualize-traces-clj")))
        s (style container)]
    [(js/parseInt (.-width s)) (js/parseInt (.-height s))]))

(defn resize []
  (q/with-sketch (q/get-sketch-by-id "visualize-traces-clj")
    (apply q/resize-sketch (sketch-size))))

(unlisten! js/window :resize resize)
(listen! js/window :resize resize)

(defn ^:export run-sketch []
  (q/defsketch visualize-traces-clj
    :host "visualize-traces-clj"
    :size (sketch-size)
    :setup setup
    :update update-state
    :draw draw-state
    :middleware [m/fun-mode]))
