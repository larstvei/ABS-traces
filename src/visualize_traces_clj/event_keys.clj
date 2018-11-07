(ns visualize-traces-clj.event-keys
  (:require [clojure.string :as s]))

(defn event-key->event [trace event-key]
  (get-in trace event-key))

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
