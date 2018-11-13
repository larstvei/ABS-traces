(ns visualize-traces-clj.dpor
  (:require [clojure.set :refer [difference]]
            [visualize-traces-clj.example-traces :refer :all]
            [visualize-traces-clj.event-keys :refer :all]))

;; 
(defn blocked-events [trace]
  (-> (fn [[cog schedule]]
        (keep-indexed
         (fn [i event]
           (when (or (= (:event-type event) :schedule)
                     (= (:event-type event) :future-read))
             [cog i])) schedule))
      (mapcat trace) set))

;; Returns a list of events in the trace that satisfy the predicate
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

;; Schedule events in the trace that are enabled by the given invocation event
(defn enabled-by-invoc [event trace]
  (enables (partial = (assoc event :event-type :schedule)) trace))

;; Future-read events in the trace that are enabled by the given completion event
(defn enabled-by-completion [event trace]
  (enables (partial = (assoc event :event-type :future-read)) trace))

;; All events that are enabled by the event with the given event key
(defn enabled-by [event-key trace]
  (let [event (get-in trace event-key)]
    (case (:event-type event)
      :invocation (enabled-by-invoc event trace)
      :completed (enabled-by-completion event trace)
      #{})))

;; All non-schedule events in the schedule until the next schedule event
(defn schedule-bulk [trace schedule]
  (-> (fn [k] (-> (event-key-type trace k) (not= :schedule)))
      (take-while schedule)))

;; The first event from each cog (with regards to the task number)
(defn one-per-cog [event-keys]
  (let [cogs (group-by first event-keys)]
    (set (map (comp first (partial sort-by second)) (vals cogs)))))

;; A set of lists, where each list represents a "schedule run" for a cog. The first element
;; in the schedule run is a schedule event for a task, and the following events are possible
;; invocations done by the task, followed by a completion event for the task.
;;
;; Note: Assumes that the first event in all schedules (i.e. x below) is a schedule event
(defn one-schedule-run-per-cog [trace event-keys]
  (let [cogs (group-by first event-keys)]
    (reduce (fn [res [cog schedule]]
              (let [[x & xs] (sort-by second schedule)
                    non-schedule-events (schedule-bulk trace xs)]
                (conj res (conj non-schedule-events x))))
            #{}
            cogs)))

;; A list of lists, where each list starts with a schedule event, followed by possible
;; invocation events and a completion event for the task that was scheduled.
(defn schedule-runs [trace event-keys]
  (when-not (empty? event-keys)
    (let [[x & xs] event-keys
          [run ys] (split-with (comp (partial not= :schedule)
                                     (partial event-key-type trace))
                               xs)]
      (cons (cons x run) (schedule-runs trace ys)))))

;; Returns the blocked events, but makes sure to remove main and init from the
;; returned value. A special case is needed for these events, seeing as they are
;; the first events in each cog, and normally an event is unblocked by another
;; event being done.
(defn unblock-init [trace]
  (let [inits (set (map (fn [cog] [cog 0]) (keys trace)))]
    (difference (blocked-events trace) inits)))

;; Makes event keys for a cog consisting of the cog's id followed by a number
;; between 0 and the number of events in the schedule
(defn cog-local-trace->event-keys [cog schedule]
  (for [i (range (count schedule))]
    [cog i]))

;; Produces event keys for each cog in the trace
(defn trace->event-keys [trace]
  (-> (fn [res [cog schedule]]
        (into res (cog-local-trace->event-keys cog schedule)))
      (reduce #{} trace)))

;; Attempts to calculcate the process queue at each stage in the execution,
;; giving the result as a tree
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

;; Calculates all possible known paths in the execution from a given trace
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
       (->> (for [x (remove (comp blocked first) candidates)
                  :let [[c1 i1] (first x)
                        enabled (set (mapcat #(enabled-by % trace) x))]]
              (if-not (empty? (filter (fn [[c2 i2]] (and (= c1 c2) (> i1 i2))) pending))
                (list (list (list [c1 i1])))
                (map (partial cons x)
                     (trace->process-paths
                      trace
                      (difference blocked enabled)
                      (difference pending (set x))))))
            (apply concat))))))

;; A list consisting of process queues at each step of execution in a trace
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

;; Converts a trace to a history where each point in the history is a schedule run
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

;; Converts a trace to a global history where each point in the history
;; is at most one event from each cog
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

;; Converts a global history to a trace for each cog
(defn history->trace [trace history]
  (-> (fn [res event-keys]
        (-> (fn [res2 [cog i]]
              (let [e (get-in trace [cog i])]
                (update res2 cog (fnil conj []) e)))
            (reduce res event-keys)))
      (reduce {} history)))
