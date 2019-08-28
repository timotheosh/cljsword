(ns cljsword.core
  (:require [clojure.java.io :as io]
            [cljsword.config :as config]
            [cljsword.install-manager :as install-manager])
  (:import
   [org.crosswire.jsword.book.sword
    SwordBookPath]))

(defn set-sword-path
  ([] (set-sword-path :system))
  ([conf]
   (if (= conf :system)
     (SwordBookPath/setAugmentPath (into-array (config/get-system-sword-paths)))
     (SwordBookPath/setAugmentPath (into-array [(io/file conf)])))))

