(ns cljsword.core
  (import
   [org.crosswire.common.xml
    Converter
    SAXEventProvider
    XMLUtil]
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

      ;;; Key key = book.getKey(reference);
      ;;; BookData data = new BookData(book, key)
      ;;; return OSISUtil.getCanonicalText(data.getOsisFragment());

(defn getOsis
  "Obtain a SAX event provider for the OSIS document representation of
  one or more book entries."
  [version reference keycount]
  (if (and version reference)
    (let [book (getBook version)
          key (if (.equals BookCategory/BIBLE (.getBookCategory book))
                (let [key (.getKey book reference)]
                  (let [trimv (.trimVerses key keycount)]
                    (if (nil? trimv)
                      key
                      trimv)))
                (do
                  (def key (.createEmptyKeyList book))
                  (for [ aKey (subvec (.getKey(reference)) 0 keycount)]
                    (.addAll key aKey))))]
      (doto (new BookData book key) (.getSAXEventProvider)))))

(defn readStyledText
  "Obtain styled text (in this case HTML) for a book reference."
  [version reference keycount]
  (let [book (getBook version)
        osissep (getOsis version reference keycount)]
    (if osissep
      (let [styler (. ConverterFactory (getConverter))
            htmlsep (.convert styler osissep)
            bmd (.getBookMetaData book)
            direction (.isLeftToRight bmd)]
        (.setParameter htmlsep "direction" (if direction "ltr" "rtl"))
        (.writeToString XMLUtil htmlsep)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (getText "NASB" "Pro 15:2"))
