(ns cljsword.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [cljsword.config :as config]
            [cljsword.install-manager :as install]
            [cljsword.core :refer :all]))

(def install-dir "test/sword")

(defn delete-recursively [fname]
  (let [func (fn [func f]
               (when (.isDirectory f)
                 (doseq [f2 (.listFiles f)]
                   (func func f2)))
               (clojure.java.io/delete-file f))]
    (func func (clojure.java.io/file fname))))

(defn is-in-install-dir?
  [swordbook]
  (let [mod-path
        (clojure.string/join
         "/"
         (take
          2
          (take-last
           4
           (clojure.string/split
            (.toString
             (.getConfigFile (.getBookMetaData swordbook))) #"/"))))]
    (= mod-path install-dir)))

(deftest install-manager
  (testing "Make sure the install-manager handles remote book data correctly."
    (let [site "CrossWire"
          books (install/sorted-book-data-site site)]
      (is (count (install/get-available-books site))
          (reduce +
                  (map (fn [x] (count (x books))) (keys books)))))))

(deftest install-bible
  (.mkdir (io/file install-dir))
  (config/set-installation-path install-dir)
  (install/install-book "KJV" "CrossWire")
  (testing "Make sure we installed a module and it exists."
    (is (.isFile (io/file (str install-dir "/mods.d/kjv.conf")))))
  (testing "Get the name of a valid key for the installed book."
    (is (.getName (.getKey (get-book "KJV") "ge3:15")) "Genesis 3:15")))

(deftest lookup-verse
  (testing "Look up a passage."
    (is (get-text "KJV" "ps118:1") "O give thanks unto the Lord ; for he is good : because his mercy endureth for ever .")))

(deftest final-step
  (delete-recursively install-dir))
