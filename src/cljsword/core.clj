(ns cljsword.core
  (:require [clojure.java.io :as io]
            [cljsword.config :as config])
  (:import
   [org.crosswire.common.xml
    XMLUtil]
   [org.crosswire.jsword.book
    BookData
    BookFilters
    Books
    OSISUtil]
   [org.crosswire.jsword.util
    ConverterFactory]
   [org.crosswire.jsword.versification.system
    Versifications]))

(defn available-books
  "Returns a list of available Book objects that are in the given category.
  'Biblical Texts'  for Bibles
  'Commentaries' for Commentaries
  'Dictionaries' for Dictionaries
  'General Books' for Books"
  [category]
  (filter
   (fn [x]
     (= category (str (.getBookCategory (.getBookMetaData x)))))
   (.getBooks (Books/installed))))

(defn get-versification
  "Returns a String representing the type of versification of a given text.
  If versification is not specified for the given text, KJV is the default."
  [text]
  (let [ins (Versifications/instance)
        v-type (.getVersification ins text)]
    (if-not (nil? v-type)
      (.getName v-type)
      "KJV")))

(defn get-books
  "Returns a list of BibleBooks from a given version. The version must be
  able to be retrieved from JSword's Versification class."
  [version]
  (let [ins (Versifications/instance)
        v-type (.getVersification ins version)]
    (map #(str %) (iterator-seq (.getBookIterator v-type)))))

(defn get-book
  "Retrieves the specified Bible version by initials (specified in the
  mods.d file)"
  [bookInitials]
  (let [books (Books/installed)]
    (.getBook books bookInitials)))

(defn get-text
  "Returns a passage from the specified version and reference."
  [version reference]
  (let [book (get-book version)]
    (if book
      (let [key (.getKey book reference)
            data (BookData. book key)]
        (OSISUtil/getCanonicalText (.getOsisFragment data))))))

(defn get-osis
  "Obtain a SAX event provider for the OSIS document representation of
  one or more book entries."
  [version reference keycount]
  (when (and version reference)
    (let [book (get-book version)
          vkey (let [vkey (.getKey book reference)
                     trimv (.trimVerses vkey keycount)]
                 (if (nil? trimv)
                   vkey
                   trimv))
          data (new BookData book vkey)]
      (.getSAXEventProvider data))))

(defn read-styled-text
  "Obtain styled text (in this case HTML) for a book reference."
  [version reference keycount]
  (let [styler (ConverterFactory/getConverter)
        book (get-book version)
        osissep (get-osis version reference keycount)]
    (if osissep
      (let [htmlsep (.convert styler osissep)
            bmd (.getBookMetaData book)
            direction (.isLeftToRight bmd)]
        (.setParameter htmlsep "direction" (if direction "ltr" "rtl"))
        (XMLUtil/writeToString htmlsep)))))

(defn get-html
  "Return the passage in HTML"
  [version reference]
  (read-styled-text version reference 100))

(defn read-dictionary
  "While Bible and Commentary are very similar, a Dictionary is read in
  a slightly different way."
  []
  (let [dicts (.getBooks (Books/installed) (BookFilters/getDictionaries))
        dict (.get dicts 0)
        keys (.getGlobalKeyList dict)
        first-key (.next (.iterator keys))
        data (new BookData dict first-key)]
    (println "The first key in the default dictionary is " first-key)
    (println "And the text against that key is "
             (OSISUtil/getPlainText (.getOsisFragment data)))))
