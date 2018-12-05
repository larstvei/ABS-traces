(ns visualize-traces-clj.example-traces)

(def shared-buffer-example-trace
  {[0]   [{:event-type :schedule, :local-id :main}
          {:caller-id [0], :event-type :new-object, :local-id 1, :name :class-NaiveSharedBuffer-Client}
          {:caller-id [0], :event-type :new-object, :local-id 2, :name :class-NaiveSharedBuffer-Client}
          {:caller-id [0], :event-type :invocation, :local-id 2, :name :m-insert}
          {:caller-id [0], :event-type :invocation, :local-id 3, :name :m-insert}
          {:event-type :completed, :local-id :main}],
   [0 0] [{:caller-id [0], :event-type :schedule, :local-id 1, :name :init}
          {:caller-id [0], :event-type :completed, :local-id 1, :name :init}
          {:caller-id [0], :event-type :schedule, :local-id 2, :name :m-insert}
          {:caller-id [0 0], :event-type :invocation, :local-id 0, :name :m-receive}
          {:caller-id [0], :event-type :completed, :local-id 2, :name :m-insert}
          {:caller-id [0 1], :event-type :schedule, :local-id 0, :name :m-receive}
          {:caller-id [0 1], :event-type :completed, :local-id 0, :name :m-receive}],
   [0 1] [{:caller-id [0], :event-type :schedule, :local-id 2, :name :init}
          {:caller-id [0], :event-type :completed, :local-id 2, :name :init}
          {:caller-id [0], :event-type :schedule, :local-id 3, :name :m-insert}
          {:caller-id [0 1], :event-type :invocation, :local-id 0, :name :m-receive}
          {:caller-id [0], :event-type :completed, :local-id 3, :name :m-insert}
          {:caller-id [0 0], :event-type :schedule, :local-id 0, :name :m-receive}
          {:caller-id [0 0], :event-type :completed, :local-id 0, :name :m-receive}]})

(def task-selection-paper-example-trace
  {[0]   [{:event-type :schedule, :local-id :main}
          {:caller-id [0], :event-type :new-object, :local-id 1, :name :class-Dpor-Reg}
          {:caller-id [0], :event-type :new-object, :local-id 2, :name :class-Dpor-Worker1}
          {:caller-id [0], :event-type :new-object, :local-id 3, :name :class-Dpor-Worker2}
          {:caller-id [0], :event-type :invocation, :local-id 0, :name :m-p}
          {:caller-id [0], :event-type :invocation, :local-id 1, :name :m-q}
          {:caller-id [0], :event-type :invocation, :local-id 2, :name :m-h}
          {:event-type :completed, :local-id :main}],
   [0 0] [{:caller-id [0], :event-type :schedule, :local-id 1, :name :init}
          {:caller-id [0], :event-type :completed, :local-id 1, :name :init}
          {:caller-id [0], :event-type :schedule, :local-id 0, :name :m-p}
          {:caller-id [0], :event-type :completed, :local-id 0, :name :m-p}
          {:caller-id [0 1], :event-type :schedule, :local-id 0, :name :m-m}
          {:caller-id [0 1], :event-type :completed, :local-id 0, :name :m-m}
          {:caller-id [0 2], :event-type :schedule, :local-id 0, :name :m-t}
          {:caller-id [0 2], :event-type :completed, :local-id 0, :name :m-t}],
   [0 1] [{:caller-id [0], :event-type :schedule, :local-id 2, :name :init}
          {:caller-id [0], :event-type :completed, :local-id 2, :name :init}
          {:caller-id [0], :event-type :schedule, :local-id 1, :name :m-q}
          {:caller-id [0 1], :event-type :invocation, :local-id 0, :name :m-m}
          {:caller-id [0], :event-type :completed, :local-id 1, :name :m-q}],
   [0 2] [{:caller-id [0], :event-type :schedule, :local-id 3, :name :init}
          {:caller-id [0], :event-type :completed, :local-id 3, :name :init}
          {:caller-id [0], :event-type :schedule, :local-id 2, :name :m-h}
          {:caller-id [0 2], :event-type :invocation, :local-id 0, :name :m-t}
          {:caller-id [0], :event-type :completed, :local-id 2, :name :m-h}]})
