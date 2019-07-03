(ns visualize-traces-clj.event-keys
  (:require [clojure.string :as s]))

(defn event-key->event [trace event-key]
  (get-in trace event-key))

(defn event-key-type [trace event-key]
  (get-in trace (conj event-key :type)))

(defn event-key-time [trace event-key]
  (get-in trace (conj event-key :time)))

(defn event-key-method [trace event-key]
  (let [name (get-in trace (conj event-key :name))]
    (if (or (nil? name) (= name :undefined))
      (get-in trace (conj event-key :local_id))
      name)))

(defn event-key-method-name [trace event-key]
  (let [method (event-key-method trace event-key)]
    (s/replace-first (name method) "m_" "")))

(defn event-key-task [trace event-key]
  (let [event (event-key->event trace event-key)]
    [(or (:caller_id event) (first event-key)) (:local_id event)]))
