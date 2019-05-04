(defproject cljsword "0.2.0-SNAPSHOT"
  :description ""
  :url "https://github.com/timotheosh/cljsword"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clojurewerkz/propertied "1.3.0"]
                 [clojure-ini "0.0.2"]
                 [aero "1.1.3"]
                 [org.jdom/jdom2 "2.0.4"]
                 [org.crosswire/jsword "2.1-SNAPSHOT"]]
  :repl-options {:init-ns cljsword.core})
