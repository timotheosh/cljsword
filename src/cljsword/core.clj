(ns cljsword.core
  (import [org.crosswire.jsword.book
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


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (getText "NASB" "Pro 15:2"))
