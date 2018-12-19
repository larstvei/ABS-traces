(ns visualize-traces-clj.example-traces)

(def shared-buffer-example-trace
  {[0]   [{:type :schedule,:local-id :main}
          {:caller-id [0], :type :new-object, :local-id 1, :name :class-NaiveSharedBuffer-Client}
          {:caller-id [0], :type :new-object, :local-id 2, :name :class-NaiveSharedBuffer-Client}
          {:caller-id [0], :type :invocation, :local-id 2, :name :m-insert}
          {:caller-id [0], :type :invocation, :local-id 3, :name :m-insert}
          {:type :future-write, :local-id :main}],
   [0 0] [{:caller-id [0], :type :schedule, :local-id 1, :name :init}
          {:caller-id [0], :type :future-write, :local-id 1, :name :init}
          {:caller-id [0], :type :schedule, :local-id 2, :name :m-insert}
          {:caller-id [0 0], :type :invocation, :local-id 0, :name :m-receive}
          {:caller-id [0], :type :future-write, :local-id 2, :name :m-insert}
          {:caller-id [0 1], :type :schedule, :local-id 0, :name :m-receive}
          {:caller-id [0 1], :type :future-write, :local-id 0, :name :m-receive}],
   [0 1] [{:caller-id [0], :type :schedule, :local-id 2, :name :init}
          {:caller-id [0], :type :future-write, :local-id 2, :name :init}
          {:caller-id [0], :type :schedule, :local-id 3, :name :m-insert}
          {:caller-id [0 1], :type :invocation, :local-id 0, :name :m-receive}
          {:caller-id [0], :type :future-write, :local-id 3, :name :m-insert}
          {:caller-id [0 0], :type :schedule, :local-id 0, :name :m-receive}
          {:caller-id [0 0], :type :future-write, :local-id 0, :name :m-receive}]})

(def task-selection-paper-example-trace
  {[0]   [{:type :schedule, :local-id :main}
          {:caller-id [0], :type :new-object, :local-id 1, :name :class-Dpor-Reg}
          {:caller-id [0], :type :new-object, :local-id 2, :name :class-Dpor-Worker1}
          {:caller-id [0], :type :new-object, :local-id 3, :name :class-Dpor-Worker2}
          {:caller-id [0], :type :invocation, :local-id 0, :name :m-p}
          {:caller-id [0], :type :invocation, :local-id 1, :name :m-q}
          {:caller-id [0], :type :invocation, :local-id 2, :name :m-h}
          {:type :future-write, :local-id :main}],
   [0 0] [{:caller-id [0], :type :schedule, :local-id 1, :name :init}
          {:caller-id [0], :type :future-write, :local-id 1, :name :init}
          {:caller-id [0], :type :schedule, :local-id 0, :name :m-p}
          {:caller-id [0], :type :future-write, :local-id 0, :name :m-p}
          {:caller-id [0 1], :type :schedule, :local-id 0, :name :m-m}
          {:caller-id [0 1], :type :future-write, :local-id 0, :name :m-m}
          {:caller-id [0 2], :type :schedule, :local-id 0, :name :m-t}
          {:caller-id [0 2], :type :future-write, :local-id 0, :name :m-t}],
   [0 1] [{:caller-id [0], :type :schedule, :local-id 2, :name :init}
          {:caller-id [0], :type :future-write, :local-id 2, :name :init}
          {:caller-id [0], :type :schedule, :local-id 1, :name :m-q}
          {:caller-id [0 1], :type :invocation, :local-id 0, :name :m-m}
          {:caller-id [0], :type :future-write, :local-id 1, :name :m-q}],
   [0 2] [{:caller-id [0], :type :schedule, :local-id 3, :name :init}
          {:caller-id [0], :type :future-write, :local-id 3, :name :init}
          {:caller-id [0], :type :schedule, :local-id 2, :name :m-h}
          {:caller-id [0 2], :type :invocation, :local-id 0, :name :m-t}
          {:caller-id [0], :type :future-write, :local-id 2, :name :m-h}]})

