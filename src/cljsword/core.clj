(ns cljsword.core
  (import
   [org.crosswire.common.util
    NetUtil
    ResourceUtil]
   [org.crosswire.common.xml
    Converter
    SAXEventProvider
    XMLUtil
    TransformingSAXEventProvider]
   [org.crosswire.jsword.book
    Book
    BookCategory
    BookData
    BookException
    BookFilter
    BookFilters
    BookMetaData;
    Books
    BooksEvent
    BooksListener
    OSISUtil]
   [org.crosswire.jsword.book.install
    InstallException
    InstallManager
    Installer]
   [org.crosswire.jsword.index.search
    DefaultSearchModifier
    DefaultSearchRequest]
   [org.crosswire.jsword.passage
    Key
    NoSuchKeyException
    Passage
    PassageTally
    RestrictionType
    VerseRange]
   [org.crosswire.jsword.util
    ConverterFactory])
  (:gen-class))

(def BIBLE_NAME (str "KJV"))

(defn getBook
  "Retrieves the specified Bible version by initials (specified in the
  mods.d file)"
  [bookInitials]
  (let [books (Books/installed)]
    (.getBook books bookInitials)))

(defn getText
  "Returns a passage from the specified version and reference."
  [version reference]
  (let [book (getBook version)]
    (if book
      (let [key (.getKey book reference)
            data (BookData. book key)]
        (OSISUtil/getCanonicalText (.getOsisFragment data))))))

(defn getOsis
  "Obtain a SAX event provider for the OSIS document representation of
  one or more book entries."
  [version reference keycount]
  (if (and version reference)
    (let [book (getBook version)
          vkey (if (.equals BookCategory/BIBLE (.getBookCategory book))
                (let [vkey (.getKey book reference)]
                  (let [trimv (.trimVerses vkey keycount)]
                    (if (nil? trimv)
                      vkey
                      trimv)))
                (do
                  (def vkey (.createEmptyKeyList book))
                  (for [ aKey (subvec (.getKey(reference)) 0 keycount)]
                    (.addAll vkey aKey))))
          data (new BookData book vkey)]
      (.getSAXEventProvider data))))

(defn readStyledText
  "Obtain styled text (in this case HTML) for a book reference."
  [version reference keycount]
  (let [styler (. ConverterFactory (getConverter))
        book (getBook version)
        osissep (getOsis version reference keycount)]
    (if osissep
      (let [htmlsep (.convert styler osissep)
            bmd (.getBookMetaData book)
            direction (.isLeftToRight bmd)]
        (.setParameter htmlsep "direction" (if direction "ltr" "rtl"))
        (XMLUtil/writeToString htmlsep)))))

(defn readDictionary
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

(defn search
  "An example of how to search for various bits of data."
  []
  (let [bible (.getBook (Books/installed) BIBLE_NAME)
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

(defn rankedSearch
  "TODO: Still does not work. Can't call Java enum
   An example of how to perform a ranked search."
  []
  (let [bible (.getBook (Books/installed) BIBLE_NAME)
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
          (when [(and (> rankCount 0)
                      (< rankCount total))]
            (doall (.trimRanges tally rankCount RestrictionType/NONE)
                   (println "Showing the first " rankCount
                            " of " total " verses.")))))
      (println results))))

(defn searchAndShow
  "An example of how to do a search and then get text for each range of
  verses."
  []
  (let [bible (.getBook (Books/installed) BIBLE_NAME)
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

(defn -main
    "I don't do a whole lot ... yet."
  [& args]
  (println (readStyledText "NASB" "Pro 15:2" 100)))
