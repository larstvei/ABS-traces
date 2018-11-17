(ns visualize-traces-clj.dpor
  "Contains functions used to perform Dynamic Partial Order Reduction (DPOR) on ABS models."
  (:require [clojure.set :refer [difference]]
            [visualize-traces-clj.example-traces :refer :all]
            [visualize-traces-clj.event-keys :refer :all]))

(defn blocked-events
  "Returns a sequence of the events in `trace` that are initially blocked."
  [trace]
  (-> (fn [[cog schedule]]
        (keep-indexed
         (fn [i event]
           (when (or (= (:event-type event) :schedule)
                     (= (:event-type event) :future-read))
             [cog i])) schedule))
      (mapcat trace) set))

(defn enables
  "Returns a sequence of the events in `trace` that satisfy `pred`."
  [pred trace]
  (->> trace
       (keep (fn [[cog schedule]]
               (->> schedule
                    (map-indexed vector)
                    (filter (comp pred second))
                    (map first)
                    (map (partial vector cog))
                    (not-empty))))
       (apply concat)))

(defn enabled-by-invoc
  "Returns a sequence of the schedule events in `trace` that are enabled by
  `event`, which is expected to be an invocation event."
  [event trace]
  (enables (partial = (assoc event :event-type :schedule)) trace))

(defn enabled-by-completion
  "Returns a sequence of the future-read events in `trace` that are enabled by
  `event`, which is expected to be a completion event."
  [event trace]
  (enables (partial = (assoc event :event-type :future-read)) trace))

(defn enabled-by
  "Returns a sequence of the events that are enabled by the event identified by
  `event-key` in `trace`."
  [event-key trace]
  (let [event (get-in trace event-key)]
    (case (:event-type event)
      :invocation (enabled-by-invoc event trace)
      :completed (enabled-by-completion event trace)
      #{})))

(defn schedule-bulk
  "Returns a sequence of the non-schedule events in `schedule` until the next
  schedule event in `trace`."
  [trace schedule]
  (-> (fn [k] (-> (event-key-type trace k) (not= :schedule)))
      (take-while schedule)))

(defn one-per-cog
  "Returns a sequence of the first event from each cog."
  [event-keys]
  (let [cogs (group-by first event-keys)]
    (set (map (comp first (partial sort-by second)) (vals cogs)))))

(defn one-schedule-run-per-cog
  "Returns a set of lists, where each list represents a 'schedule run' for a cog
  in `trace` identified by `event-keys`. The first element in each schedule run
  is a schedule event for a task, and the following events are possible
  invocations done by the task, followed by a completion event for the task.
  
  Note: Assumes that the first event in all schedules is a schedule event."
  [trace event-keys]
  (let [cogs (group-by first event-keys)]
    (reduce (fn [res [cog schedule]]
              (let [[x & xs] (sort-by second schedule)
                    non-schedule-events (schedule-bulk trace xs)]
                (conj res (conj non-schedule-events x))))
            #{}
            cogs)))

(defn schedule-runs
  "Returns a sequence of lists, where each list represents a 'schedule run' for
  a cog in `trace` identified by `event-keys`. The first element in each
  schedule run is a schedule event for a task, and the following events are
  possible invocations done by the task, followed by a completion event for the
  task.
  
  Note: Assumes that the first event in all schedules is a schedule event."
  [trace event-keys]
  (when-not (empty? event-keys)
    (let [[x & xs] event-keys
          [run ys] (split-with (comp (partial not= :schedule)
                                     (partial event-key-type trace))
                               xs)]
      (cons (cons x run) (schedule-runs trace ys)))))

(defn unblock-init
  "Returns a sequence of the blocked events in `trace`, but makes sure to remove
  main and init from the returned value. A special case is needed for these
  events, seeing as they are the first events in each cog, and normally an event
  is unblocked by another event being done."
  [trace]
  (let [inits (set (map (fn [cog] [cog 0]) (keys trace)))]
    (difference (blocked-events trace) inits)))


(defn cog-local-trace->event-keys
  "Returns a sequence of event keys based on a `cog` and its `schedule`, where
  each key consists of `cog`'s id and a number between 0 and the size of
  `schedule`."
  [cog schedule]
  (for [i (range (count schedule))]
    [cog i]))

(defn trace->event-keys
  "Returns a sequence of event keys for each cog in `trace`."
  [trace]
  (-> (fn [res [cog schedule]]
        (into res (cog-local-trace->event-keys cog schedule)))
      (reduce #{} trace)))

#_(defn trace->queue-tree
    "Attempts to calculate the process queue at each stage in the execution,
    giving the result as a tree."
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
  "Returns a sequence of all possible known paths in the execution from `trace`."
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

(defn trace->process-queues
  "Returns a list consisting of process quques at each step of the execution
  for `trace`."
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
  "Returns a global history corresponding to `trace`, where each point in the
  history is a schedule run."
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
  "Returns a global history corresponding to `trace`."
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
  "Returns a trace corresponding to `history`. Its intented use is to convert
  a global history back to a trace, after first having converted a trace to this
  history. Since the history does not contain all information about each event,
  the original trace is required to make an accurate conversion."
  (-> (fn [res event-keys]
        (-> (fn [res2 [cog i]]
              (let [e (get-in trace [cog i])]
                (update res2 cog (fnil conj []) e)))
            (reduce res event-keys)))
      (reduce {} history)))
