(ns visualize-traces-clj.core
  (:gen-class)
  (:require [clj-http.client :as h]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [visualize-traces-clj.dpor :refer :all]
            [visualize-traces-clj.draw :refer [make-state draw-state setup]]
            [visualize-traces-clj.example-traces :refer :all]))

(defn json->trace [s]
  (->> (clojure.walk/prewalk (fn [x] (if (string? x) (keyword x) x)) s)
       (reduce (fn [res cog] (assoc res (:cog_id cog) (:cog_schedule cog))) {})))

(defn get-trace-from-simulator []
  (try
    (-> (h/get "http://localhost:8080/trace" {:accept :json})
        :body json/read-json json->trace)
    (catch Exception e nil)))

(defn get-db-traces-from-simulator []
  (try
    (->> (h/get "http://localhost:8080/db_traces" {:accept :json})
         :body json/read-json (map json->trace))
    (catch Exception e nil)))

(defn advance-simulator-clock
  ([] (advance-simulator-clock 1))
  ([n] (try
         (-> (str "http://localhost:8080/clock/advance?by=" n)
             (h/get {:accept :json}))
         (get-trace-from-simulator)
         (catch Exception e nil))))

(defn key-handler [{:keys [:states :current :height :start] :as app-state} event]
  (let [state (states current)
        n (count (:history state))]
    (case (q/key-as-keyword)
      :? (update-in app-state :help not)

      :up (update app-state :start (comp (partial max 0) dec))

      :down (let [m (- n height)]
              (update app-state :start (comp (partial min m) inc)))

      :- (update app-state :height (comp (partial max 1) dec))

      :+ (update app-state :height (comp (partial min n) inc))

      := (update app-state :height (comp (partial min n) inc))

      :l (if-let [new-trace (get-trace-from-simulator)]
           (setup [new-trace])
           app-state)

      :c (let [t (advance-simulator-clock)
               new-state (make-state t)]
           (-> app-state
               (assoc :states [new-state])
               (assoc :current 0)))

      :d (if-let [new-traces (not-empty (get-db-traces-from-simulator))]
           (setup new-traces)
           (setup [{}]))

      :left (assoc app-state :current (mod (dec current) (count states)))

      :right (assoc app-state :current (mod (inc current) (count states)))

      :s (do (q/save-frame "#######.png")
             app-state)

      app-state)))

(defn ^:export run-sketch []
  (q/defsketch visualize-traces-clj
    :host "visualize-traces-clj"
    :size [800 1000]
    :setup (partial setup [{}])
    :key-pressed key-handler
    :draw draw-state
    :settings (partial q/pixel-density 2)
    :features [:resizable :no-bind-output]
    :middleware [m/fun-mode]))

(defn -main [& args]
  (run-sketch))

#_(run-sketch)
