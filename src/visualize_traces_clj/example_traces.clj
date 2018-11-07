(ns visualize-traces-clj.example-traces)

(def example-trace
  {[0]   [{:event-type :schedule, :task-id :main}
          {:caller-id [0], :event-type :invocation, :method :m-insert, :task-id 2}
          {:caller-id [0], :event-type :invocation, :method :m-insert, :task-id 3}]
   [0 0] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-insert, :task-id 2}
          {:caller-id [0 0], :event-type :invocation, :method :m-receive, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-insert, :task-id 2}
          {:caller-id [0 1], :event-type :schedule, :method :m-receive, :task-id 0}
          {:caller-id [0 1], :event-type :completed, :method :m-receive, :task-id 0}]
   [0 1] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-insert, :task-id 3}
          {:caller-id [0 1], :event-type :invocation, :method :m-receive, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-insert, :task-id 3}
          {:caller-id [0 0], :event-type :schedule, :method :m-receive, :task-id 0}
          {:caller-id [0 0], :event-type :completed, :method :m-receive, :task-id 0}]})

(def example-trace2
  {[0]   [{:event-type :schedule, :task-id :main}
          {:caller-id [0], :event-type :invocation, :method :m-p, :task-id 0}
          {:caller-id [0], :event-type :invocation, :method :m-q, :task-id 1}
          {:caller-id [0], :event-type :invocation, :method :m-h, :task-id 2}],
   [0 0] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-p, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-p, :task-id 0}
          {:caller-id [0 1], :event-type :schedule, :method :m-m, :task-id 0}
          {:caller-id [0 1], :event-type :completed, :method :m-m, :task-id 0}
          {:caller-id [0 2], :event-type :schedule, :method :m-t, :task-id 0}
          {:caller-id [0 2], :event-type :completed, :method :m-t, :task-id 0}],
   [0 1] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-q, :task-id 1}
          {:caller-id [0 1], :event-type :invocation, :method :m-m, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-q, :task-id 1}],
   [0 2] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-h, :task-id 2}
          {:caller-id [0 2], :event-type :invocation, :method :m-t, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-h, :task-id 2}]})

(def example-trace3
  {[0]   [{:event-type :schedule, :task-id :main}
          {:caller-id [0], :event-type :invocation, :method :m-work, :task-id 0}
          {:caller-id [0], :event-type :invocation, :method :m-abort, :task-id 1}],
   [0 0] [{:event-type :schedule, :task-id :init}
          {:caller-id [0], :event-type :schedule, :method :m-work, :task-id 0}
          {:caller-id [0 0], :event-type :invocation, :method :m-work, :task-id 0}
          {:caller-id [0], :event-type :completed, :method :m-work, :task-id 0}
          {:caller-id [0], :event-type :schedule, :method :m-abort, :task-id 1}
          {:caller-id [0], :event-type :completed, :method :m-abort, :task-id 1}
          {:caller-id [0 0], :event-type :schedule, :method :m-work, :task-id 0}
          {:caller-id [0 0], :event-type :completed, :method :m-work, :task-id 0}]})

(def example-trace3-wanted-result
  #{{[0]   [{:event-type :schedule, :task-id :main}
            {:caller-id [0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :invocation, :method :m-abort, :task-id 1}],
     [0 0] [{:event-type :schedule, :task-id :init}
            {:caller-id [0], :event-type :schedule, :method :m-work, :task-id 0}
            {:caller-id [0 0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :completed, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :schedule, :method :m-abort, :task-id 1}
            {:caller-id [0], :event-type :completed, :method :m-abort, :task-id 1}
            {:caller-id [0 0], :event-type :schedule, :method :m-work, :task-id 0}
            {:caller-id [0 0], :event-type :completed, :method :m-work, :task-id 0}]}

    {[0]   [{:event-type :schedule, :task-id :main}
            {:caller-id [0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :invocation, :method :m-abort, :task-id 1}],
     [0 0] [{:event-type :schedule, :task-id :init}
            {:caller-id [0], :event-type :schedule, :method :m-abort, :task-id 1}]}

    {[0]   [{:event-type :schedule, :task-id :main}
            {:caller-id [0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :invocation, :method :m-abort, :task-id 1}],
     [0 0] [{:event-type :schedule, :task-id :init}
            {:caller-id [0], :event-type :schedule, :method :m-work, :task-id 0}
            {:caller-id [0 0], :event-type :invocation, :method :m-work, :task-id 0}
            {:caller-id [0], :event-type :completed, :method :m-work, :task-id 0}
            {:caller-id [0 0], :event-type :schedule, :method :m-work, :task-id 0}]}})
