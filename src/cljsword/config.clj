(ns cljsword.config
  (:require [clojure-ini.core :refer [read-ini]]
            [clojure.java.io :as io])
  (:import [org.crosswire.jsword.book.sword
            SwordBookPath]))

(def BIBLE_NAME (str "KJV"))

(defn- valid-path?
  "Returns true if given path is file or directory."
  [path]
  (.exists (clojure.java.io/as-file path)))

(defn get-system-sword-paths
  "Returns a vector of io/file paths for Sword, using the default places."
  []
  (filterv
   #(not (nil? %))
   [(when (valid-path? (str (System/getenv "HOME") "/.sword"))
      (io/file (str (System/getenv "HOME") "/.sword/")))
    (when (valid-path? "/usr/local/etc/sword.conf")
      (io/file (:DataPath
                (:Install
                 (read-ini "/usr/local/etc/sword.conf" :keywordize? true)))))
    (when (valid-path? "/etc/sword.conf")
      (io/file (:DataPath
                (:Install
                 (read-ini "/etc/sword.conf" :keywordize? true)))))]))

(defn add-module-path
  "Adds a path where sword modules can be found."
  [sword-path]
  (SwordBookPath/setAugmentPath (into-array [(io/file sword-path)])))

(defn set-installation-path
  "Sets the path to install new modules."
  [install-path]
  (let [sword-paths (into [] (SwordBookPath/getAugmentPath))
        install-dir (io/file install-path)]
    (when (.canWrite install-dir)
      (when-not (some #(= install-path %)
                      (map #(.toString %) sword-paths))
        (add-module-path install-path))
      (SwordBookPath/setDownloadDir install-dir))))