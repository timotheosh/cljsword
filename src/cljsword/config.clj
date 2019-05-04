(ns cljsword.config
  (:require [aero.core :refer [read-config]]
            [clojure-ini.core :refer [read-ini]]
            [clojure.java.io :as io]))

(defn- valid-path?
  "Returns true if given path is file or directory."
  [path]
  (.exists (clojure.java.io/as-file path)))

(defn get-sword-path-from-config
  "Returns a vector of io/file paths for Sword found in this library's config file."
  ([] (get-sword-path-from-config "config.edn"))
  ([config-file]
   (mapv
    (fn [x] (-> x io/resource io/file))
    (:home (:sword (read-config (io/resource "config.edn")))))))

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
