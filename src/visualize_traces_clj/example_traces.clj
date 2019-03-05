(ns visualize-traces-clj.example-traces)

(def shared-buffer-example-trace
  {[0]   [{:type :schedule,:local_id :main}
          {:caller_id [0], :type :new_object, :local_id 1, :name :class-NaiveSharedBuffer-Client}
          {:caller_id [0], :type :new_object, :local_id 2, :name :class-NaiveSharedBuffer-Client}
          {:caller_id [0], :type :invocation, :local_id 2, :name :m-insert}
          {:caller_id [0], :type :invocation, :local_id 3, :name :m-insert}
          {:type :future_write, :local_id :main}],
   [0 0] [{:caller_id [0], :type :schedule, :local_id 1, :name :init}
          {:caller_id [0], :type :future_write, :local_id 1, :name :init}
          {:caller_id [0], :type :schedule, :local_id 2, :name :m-insert}
          {:caller_id [0 0], :type :invocation, :local_id 0, :name :m-receive}
          {:caller_id [0], :type :future_write, :local_id 2, :name :m-insert}
          {:caller_id [0 1], :type :schedule, :local_id 0, :name :m-receive}
          {:caller_id [0 1], :type :future_write, :local_id 0, :name :m-receive}],
   [0 1] [{:caller_id [0], :type :schedule, :local_id 2, :name :init}
          {:caller_id [0], :type :future_write, :local_id 2, :name :init}
          {:caller_id [0], :type :schedule, :local_id 3, :name :m-insert}
          {:caller_id [0 1], :type :invocation, :local_id 0, :name :m-receive}
          {:caller_id [0], :type :future_write, :local_id 3, :name :m-insert}
          {:caller_id [0 0], :type :schedule, :local_id 0, :name :m-receive}
          {:caller_id [0 0], :type :future_write, :local_id 0, :name :m-receive}]})

(def task-selection-paper-example-trace
  {[0]   [{:type :schedule, :local_id :main}
          {:caller_id [0], :type :new_object, :local_id 1, :name :class-Dpor-Reg}
          {:caller_id [0], :type :new_object, :local_id 2, :name :class-Dpor-Worker1}
          {:caller_id [0], :type :new_object, :local_id 3, :name :class-Dpor-Worker2}
          {:caller_id [0], :type :invocation, :local_id 0, :name :m-p}
          {:caller_id [0], :type :invocation, :local_id 1, :name :m-q}
          {:caller_id [0], :type :invocation, :local_id 2, :name :m-h}
          {:type :future_write, :local_id :main}],
   [0 0] [{:caller_id [0], :type :schedule, :local_id 1, :name :init}
          {:caller_id [0], :type :future_write, :local_id 1, :name :init}
          {:caller_id [0], :type :schedule, :local_id 0, :name :m-p}
          {:caller_id [0], :type :future_write, :local_id 0, :name :m-p}
          {:caller_id [0 1], :type :schedule, :local_id 0, :name :m-m}
          {:caller_id [0 1], :type :future_write, :local_id 0, :name :m-m}
          {:caller_id [0 2], :type :schedule, :local_id 0, :name :m-t}
          {:caller_id [0 2], :type :future_write, :local_id 0, :name :m-t}],
   [0 1] [{:caller_id [0], :type :schedule, :local_id 2, :name :init}
          {:caller_id [0], :type :future_write, :local_id 2, :name :init}
          {:caller_id [0], :type :schedule, :local_id 1, :name :m-q}
          {:caller_id [0 1], :type :invocation, :local_id 0, :name :m-m}
          {:caller_id [0], :type :future_write, :local_id 1, :name :m-q}],
   [0 2] [{:caller_id [0], :type :schedule, :local_id 3, :name :init}
          {:caller_id [0], :type :future_write, :local_id 3, :name :init}
          {:caller_id [0], :type :schedule, :local_id 2, :name :m-h}
          {:caller_id [0 2], :type :invocation, :local_id 0, :name :m-t}
          {:caller_id [0], :type :future_write, :local_id 2, :name :m-h}]})

(def task-selection-paper-example-trace-from-simulator
  {[0] [{:caller_id :undefined, :local_id :main, :name :undefined, :reads [], :type :schedule, :writes []}
        {:caller_id [0], :local_id 0, :name :class-ABS-DC-DeploymentComponent, :reads [], :type :new_object, :writes []}
        {:caller_id [0], :local_id 1, :name :class-Dpor-Reg, :reads [], :type :new_object, :writes []}
        {:caller_id [0], :local_id 2, :name :class-Dpor-Worker1, :reads [], :type :new_object, :writes []}
        {:caller_id [0], :local_id 3, :name :class-Dpor-Worker2, :reads [], :type :new_object, :writes []}
        {:caller_id [0], :local_id 0, :name :m-p, :reads [], :type :invocation, :writes []}
        {:caller_id [0], :local_id 1, :name :m-q, :reads [], :type :invocation, :writes []}
        {:caller_id [0], :local_id 2, :name :m-h, :reads [], :type :invocation, :writes []}
        {:caller_id :undefined, :local_id :main, :name :undefined, :reads [], :type :suspend, :writes []}
        {:caller_id :undefined, :local_id :main, :name :undefined, :reads [], :type :schedule, :writes []}
        {:caller_id [0], :local_id 3, :name :m-prn, :reads [], :type :invocation, :writes []}
        {:caller_id [0], :local_id 3, :name :m-prn, :reads [], :type :future_read, :writes []}
        {:caller_id :undefined, :local_id :main, :name :undefined, :reads [], :type :future_write, :writes []}],
   [0 0] [{:caller_id [0], :local_id 1, :name :init, :reads [], :type :schedule, :writes []}
          {:caller_id [0], :local_id 1, :name :init, :reads [], :type :future_write, :writes [:f :g]}
          {:caller_id [0], :local_id 0, :name :m-p, :reads [], :type :schedule, :writes []}
          {:caller_id [0], :local_id 0, :name :m-p, :reads [:f], :type :future_write, :writes [:f]}
          {:caller_id [0 1], :local_id 0, :name :m-m, :reads [], :type :schedule, :writes []}
          {:caller_id [0 1], :local_id 0, :name :m-m, :reads [:g], :type :future_write, :writes [:g]}
          {:caller_id [0 2], :local_id 0, :name :m-t, :reads [], :type :schedule, :writes []}
          {:caller_id [0 2], :local_id 0, :name :m-t, :reads [:g], :type :future_write, :writes [:g]}
          {:caller_id [0], :local_id 3, :name :m-prn, :reads [], :type :schedule, :writes []}
          {:caller_id [0], :local_id 3, :name :m-prn, :reads [:f :g], :type :future_write, :writes []}
          {:caller_id :undefined, :local_id :run, :name :undefined, :reads [], :type :schedule, :writes []}
          {:caller_id :undefined, :local_id :run, :name :undefined, :reads [], :type :future_write, :writes []}],
   [0 1] [{:caller_id [0], :local_id 2, :name :init, :reads [], :type :schedule, :writes []}
          {:caller_id [0], :local_id 2, :name :init, :reads [], :type :future_write, :writes []}
          {:caller_id [0], :local_id 1, :name :m-q, :reads [], :type :schedule, :writes []}
          {:caller_id [0 1], :local_id 0, :name :m-m, :reads [], :type :invocation, :writes []}
          {:caller_id [0], :local_id 1, :name :m-q, :reads [], :type :future_write, :writes []}],
   [0 2] [{:caller_id [0], :local_id 3, :name :init, :reads [], :type :schedule, :writes []}
          {:caller_id [0], :local_id 3, :name :init, :reads [], :type :future_write, :writes []}
          {:caller_id [0], :local_id 2, :name :m-h, :reads [], :type :schedule, :writes []}
          {:caller_id [0 2], :local_id 0, :name :m-t, :reads [], :type :invocation, :writes []}
          {:caller_id [0], :local_id 2, :name :m-h, :reads [], :type :future_write, :writes []}]})
