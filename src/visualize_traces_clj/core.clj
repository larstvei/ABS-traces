(ns visualize-traces-clj.core
  (:gen-class)
  (:require [clj-http.client :as h]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [visualize-traces-clj.dpor :refer :all]
            [visualize-traces-clj.draw :refer [draw-state setup]]
            [visualize-traces-clj.example-traces :refer :all]))

(defn json->trace [s]
  (->> (clojure.walk/prewalk (fn [x] (if (string? x) (keyword x) x)) s)
       (reduce (fn [res cog] (assoc res (:cog-id cog) (:cog-schedule cog))) {})))

(defn json-str->trace [s]
  (->> (s/replace s "_" "-") json/read-json json->trace))

(defn get-trace-from-simulator []
  (try
    (-> (h/get "http://localhost:8080/schedules" {:accept :json})
        :body json-str->trace)
    (catch Exception e nil)))

(defn advance-simulator-clock
  ([] (advance-simulator-clock 1))
  ([n] (try
         (-> (str "http://localhost:8080/clock/advance?by=" n)
             (h/get {:accept :json}))
         (catch Exception e nil))))

(defn key-handler [state event]
  (q/redraw)
  (let [n (count (:history state))]
    (case (q/key-as-keyword)
      :? (update state :help not)

      :up (update state :start (comp (partial max 0) dec))

      :down (let [m (- n (:height state))]
              (update state :start (comp (partial min m) inc)))

      :- (update state :height (comp (partial max 1) dec))

      :+ (update state :height (comp (partial min n) inc))

      := (update state :height (comp (partial min n) inc))

      :l (if-let [new-trace (get-trace-from-simulator)]
           (setup new-trace)
           state)

      :c (do (advance-simulator-clock)
             (let [t (get-trace-from-simulator)]
               (merge state {:trace t
                             :history (trace->history t)
                             :cogs (keys t)})))

      state)))

(defn ^:export run-sketch []
  (q/defsketch visualize-traces-clj
    :host "visualize-traces-clj"
    :size [800 1000]
    :setup (partial setup shared-buffer-example-trace)
    :key-pressed key-handler
    :draw draw-state
    :settings (partial q/pixel-density 2)
    :features [:resizable :no-bind-output]
    :middleware [m/fun-mode]))

(defn -main [& args]
  (run-sketch))

#_(run-sketch)
