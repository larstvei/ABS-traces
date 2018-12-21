(ns visualize-traces-clj.event-keys
  (:require [clojure.string :as s]))

(defn event-key->event [trace event-key]
  (get-in trace event-key))

(defn event-key-type [trace event-key]
  (get-in trace (conj event-key :type)))

(defn event-key-method [trace event-key]
  (let [name (get-in trace (conj event-key :name))]
    (if (or (nil? name) (= name :undefined))
      (get-in trace (conj event-key :local-id))
      name)))

(defn event-key-method-name [trace event-key]
  (let [method (event-key-method trace event-key)]
    (s/replace-first (name method) "m-" "")))

(defn event-key-task [trace event-key]
  (let [event (event-key->event trace event-key)]
    [(or (:caller-id event) (first event-key)) (:local-id event)]))
