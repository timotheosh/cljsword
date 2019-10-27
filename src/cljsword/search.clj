(ns cljsword.search
  (:require [cljsword.config :as config]
            [cljsword.core :as sword])
  (:import
    [org.crosswire.common.util
     NetUtil
     ResourceUtil]
    [org.crosswire.common.xml
     XMLUtil
     TransformingSAXEventProvider]
    [org.crosswire.jsword.book
     BookData
     Books]
    [org.crosswire.jsword.index
     IndexManagerFactory]
    [org.crosswire.jsword.index.search
     DefaultSearchModifier
     DefaultSearchRequest]
    [org.crosswire.jsword.passage
     Passage
     PassageTally
     RestrictionType]))

(defn- reindex
  "Re-indexes installed books."
  [book-initials]
  (let [book (sword/get-book book-initials)
        indexmanager (IndexManagerFactory/getIndexManager)]
    (cond (not (.isIndexed indexmanager book)) (.scheduleIndexCreation indexmanager book)
          (.needsReindexing indexmanager book) (do (.deleteIndex indexmanager book)
                                                   (.scheduleIndexCreation indexmanager book))
          :else true)))

(defn search
  "An example of how to search for various bits of data."
  []
  (let [bible (.getBook (Books/installed) config/BIBLE_NAME)
        key (.find bible "+moses +aaron")]
    (println "The following verses contain both moses and aaron: "
             (.getName key))
    (if [(instance? Passage key)]
      (let [remaining (.trimVerses key 5)]
        (println "The first 5 verses containing both moses and aaron: "
                 (.getName key))
        (if [(not (nil? remaining))]
          (println "The rest of the verses are: "
                   (.getName remaining))
          (println "There are only 5 verses containing both moses and aaron"))))))

(defn ranked-search
  "TODO: Still does not work. Can't call Java enum
   An example of how to perform a ranked search."
  []
  (let [bible (.getBook (Books/installed) config/BIBLE_NAME)
        rank true
        max 20
        modifier (new DefaultSearchModifier)]
    (.setRanked modifier rank)
    (.setMaxResults modifier max)
    (let [results (.find bible (new DefaultSearchRequest
                                    "for god so loved the world"
                                    modifier))
          total (.getCardinality results)
          partial total]
      (when [(or (instance? PassageTally results)
                 (instance? rank results))]
        (let [tally results
              rankCount max]
          ;; TODO: Call java enum value
          ;; (.setOrdering tally (PassageTally$Order/TALLY))
          (when [(and (pos? rankCount)
                      (< rankCount total))]
            (doall (.trimRanges tally rankCount RestrictionType/NONE)
                   (println "Showing the first " rankCount
                            " of " total " verses.")))))
      (println results))))

(defn search-and-show
  "An example of how to do a search and then get text for each range of
  verses."
  []
  (let [bible (.getBook (Books/installed) config/BIBLE_NAME)
        key (.find bible "melchesidec~")
        path "org/crosswire/jsword/xml/html5.xsl"
        xslurl (ResourceUtil/getResource path)
        rangeIter (.rangeIterator key RestrictionType/CHAPTER)]
    (let [range (.next rangeIter)
          data (new BookData bible range)
          osissep (.getSAXEventProvider data)
          htmlsep (new TransformingSAXEventProvider
                       (NetUtil/toURI xslurl) osissep)
          text (XMLUtil/writeToString htmlsep)]
      (println "The html text of " (.getName range) " is " text))))
