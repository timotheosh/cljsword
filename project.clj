(defproject cljsword "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.jdom/jdom "1.1.3"]
                 [org.crosswire/jsword "2.1-SNAPSHOT"]]
  :main ^:skip-aot cljsword.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
