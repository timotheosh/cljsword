(defproject cljsword "0.3.0-SNAPSHOT"
  :description ""
  :url "https://github.com/timotheosh/cljsword"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clojure-ini "0.0.2"]
                 [org.crosswire/jsword "2.1-SNAPSHOT"]]
  :repl-options {:init-ns cljsword.core}
  :profiles {:dev {:jvm-opts ["-Duser.home=/tmp"]}
             :test {:jvm-opts ["-Duser.home=/tmp"]}})
