(defproject visualize-traces-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [quil "2.7.1"]
                 [clj-http "3.9.1"]]
  :main visualize-traces-clj.core
  :aot  [visualize-traces-clj.core])
