(ns visualize-traces-clj.core
  (:require [clojure.data.json :as json]
            [clojure.string :as s]
            [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [visualize-traces-clj.draw :refer [draw-state key-handler setup]]
            [visualize-traces-clj.example-traces :refer :all]))

(defn json->trace [s]
  (->> (clojure.walk/prewalk (fn [x] (if (string? x) (keyword x) x)) s)
       (reduce (fn [res cog] (assoc res (:cog-id cog) (:cog-schedule cog))) {})))

(defn json-str->trace [s]
  (->> (s/replace s "_" "-") json/read-json json->trace))

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
