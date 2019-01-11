(ns visualize-traces-clj.dpor
  "Contains functions used to perform Dynamic Partial Order Reduction (DPOR) on ABS models."
  (:require [clojure.set :refer [difference union]]
            [visualize-traces-clj.example-traces :refer :all]
            [visualize-traces-clj.event-keys :refer :all]))

(defn blocked-events
  "Returns a sequence of the events in `trace` that are initially blocked."
  [trace]
  (-> (fn [[cog schedule]]
        (keep-indexed
         (fn [i event]
           (when (or (and (= (:type event) :schedule)
                          (not (= :main (:local-id event))))
                     (= (:type event) :future-read))
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

(defn enabled-by-new-object
  "Returns a sequence of the schedule events in `trace` that are enabled by
  `event`, which is expected to be a new object event."
  [event trace]
  (let [event2 (-> event
                   (assoc :type :schedule)
                   (assoc :name :init))]
    (enables (partial = event2) trace)))

(defn enabled-by-invoc
  "Returns a sequence of the schedule events in `trace` that are enabled by
  `event`, which is expected to be an invocation event."
  [event trace]
  (enables (partial = (assoc event :type :schedule)) trace))

(defn enabled-by-completion
  "Returns a sequence of the future-read events in `trace` that are enabled by
  `event`, which is expected to be a completion event."
  [event trace]
  (let [event2 (-> (assoc event :type :future-read)
                   (select-keys [:type :local-id :caller-id]))
        pred (comp (partial = event2)
                   #(select-keys % [:type :local-id :caller-id]))]
    (enables pred trace)))

(defn enabled-by-init
  "Returns a set containing a schedule `event` of the run method if present,
  else return an empty set."
  [[cog _] trace]
  (let [local-trace (trace cog)]
    (keep-indexed
     (fn [i event]
       (when (= (:local-id event) :run)
         [cog i]))
     local-trace)))

(defn enabled-by
  "Returns a sequence of the events that are enabled by the event identified by
  `event-key` in `trace`."
  [event-key trace]
  (let [event (get-in trace event-key)]
    (case (:type event)
      :new-object (enabled-by-new-object event trace)
      :invocation (enabled-by-invoc event trace)
      :future-write (enabled-by-completion event trace)
      :schedule (if (= (:name event) :init)
                  (enabled-by-init event-key trace)
                  #{})
      #{})))

(defn schedule-bulk
  "Returns a sequence of the non-schedule events in `trace` at `cog` until the
  next schedule event in `trace`."
  [trace [cog id]]
  (let [schedule (drop (inc id) (trace cog))
        bulksize (-> (comp (partial not= :schedule) :type)
                     (take-while schedule) count)]
    (cons [cog id] (map (fn [i] [cog (+ id i 1)]) (range bulksize)))))

(defn schedule-bulks [[s & local-trace]]
  (when s
    (let [[r next] (-> (comp (partial not= :schedule) :type)
                       (split-with local-trace))]
      (cons (cons s r) (schedule-bulks next)))))

(defn enabled-by-schedule [event-key trace]
  (let [events (->> (schedule-bulk trace event-key)
                    (mapcat #(enabled-by % trace)) set)]
    (when-let [new-events (mapcat (partial schedule-bulk trace) events)]
      (reduce into events
              (map #(enabled-by-schedule % trace) new-events)))))

(defn until-next-schedule [trace schedule]
  (-> (fn [k] (-> (event-key-type trace k) (not= :schedule)))
      (take-while schedule)))

(defn next-event-per-cog
  "Returns a sequence of the first event from each cog."
  [event-keys]
  (let [cogs (group-by first event-keys)]
    (set (map (comp first (partial sort-by second)) (vals cogs)))))

(defn one-schedule-run-per-cog
  "Returns a set of lists, where each list represents a 'schedule run' for a cog
  in `trace` identified by `event-keys`. The first element in each schedule run
  is a schedule event for a task, and the following events are possible
  invocations and future-reads done by the task, followed by a completion event
  for the task.

  Note: Assumes that the first event in all schedules is a schedule event."
  [trace event-keys]
  (let [cogs (group-by first event-keys)]
    (reduce (fn [res [cog schedule]]
              (let [[x & xs] (sort-by second schedule)
                    non-schedule-events (until-next-schedule trace xs)]
                (conj res (conj non-schedule-events x))))
            #{}
            cogs)))

(defn schedule-runs
  "Returns a sequence of lists, where each list represents a 'schedule run' for
  a cog in `trace` identified by `event-keys`. The first element in each
  schedule run is a schedule event for a task, and the following events are
  possible invocations and future-reads done by the task, followed by a
  completion event for the task.

  Note: Assumes that the first event in all schedules is a schedule event."
  [trace event-keys]
  (when-not (empty? event-keys)
    (let [[x & xs] event-keys
          [run ys] (split-with (comp (partial not= :schedule)
                                     (partial event-key-type trace))
                               xs)]
      (cons (cons x run) (schedule-runs trace ys)))))


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
    ([trace] (let [blocked (blocked-events trace)
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
  ([trace] (let [blocked (blocked-events trace)
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
  ([trace] (let [blocked (blocked-events trace)
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
  ([trace] (let [blocked (blocked-events trace)
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
  ([trace] (let [blocked (blocked-events trace)
                 pending (trace->event-keys trace)]
             (trace->history trace blocked pending nil)))
  ([trace blocked pending history]
   (if (empty? pending)
     (reverse history)
     (let [candidates (next-event-per-cog pending)
           entries (difference candidates blocked)
           m (group-by (partial event-key-type trace) entries)
           entries (if (empty? (dissoc m :time))
                     (set (:time m))
                     (difference entries (:time m)))
           enabled (set (mapcat #(enabled-by % trace) entries))]
       (if (empty? entries)
         (do (prn "Incomplete trace?")
             (prn (count candidates) "events that can't be placed in a global history.")
             (reverse history))
         (recur trace
                (difference blocked enabled)
                (difference pending entries)
                (conj history entries)))))))

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

(defn trim-trace [trace fat]
  (reduce (fn [t [cog i]]
            (let [trim (comp vec (partial take i))]
              (update t cog trim)))
          trace fat))

(defn update-after-move [trace cog i j]
  (let [local (trace cog)
        e1 (event-key->event trace [cog i])
        e1-enables (enabled-by-schedule [cog i] trace)
        e2-enables (enabled-by-schedule [cog j] trace)]
    (if (or (not (pos? j))              ; special case for main and init
            (e2-enables [cog i]))
      trace
      (-> trace
          (assoc cog (conj (vec (take j local)) e1))
          (trim-trace (union e1-enables e2-enables))))))

(defn move-backwards [trace [cog i]]
  (let [e1 (event-key->event trace [cog i])
        local (take i (trace cog))
        j (- i (-> (comp (partial not= :schedule) :type)
                   (take-while (reverse local))
                   count inc))]
    (update-after-move trace cog i j)))

(defn generate-trace [trace e2]
  (let [new-trace (move-backwards trace e2)
        ;; e1-enables (enabled-by-schedule e1 trace)
        ;; e2-enables (enabled-by-schedule )
        ]
    new-trace))

(defn trace->dpor-traces [trace]
  ;; Idé: I hver cog c1, gå gjennom hver event e1. Hvis e1 enabler e2 som er i
  ;;      en cog c2, der c1 /= c2, flytt e2 så langt bak i c2 sin lokale trace
  ;;      som mulig. Vi kan flytte e2 bakover, så lenge e_i ikke enabler e2.
  ;;      Alt som kommer etter der e2 plasseres og alt som er enabled
  ;;      (transitivt) av e2 må droppes.
  (let [event-keys (trace->event-keys trace)]
    (loop [[r & rs] (schedule-runs trace event-keys)
           new-traces #{}]
      (if-not r
        new-traces
        (let [ts (set (mapcat #(enabled-by % trace) r))
              new (map (partial generate-trace trace) ts)]
          (recur rs (into new-traces new)))))))
