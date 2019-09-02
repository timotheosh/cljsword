(ns cljsword.install-manager
  (:import
   [org.crosswire.jsword.book
    BookFilter
    BookSet]
   [org.crosswire.jsword.book.install
    InstallException
    InstallManager
    Installer]))

(def imanager (new InstallManager))

(defn get-installers
  "Returns a list of Crosswire installers from the given InstallManager."
  ([] (get-installers imanager))
  ([install-manager]
   (keys (.getInstallers install-manager))))

(defn get-available-books
  "Returns a list of available books for a given installer."
  [installer-name]
  (let [installer (.getInstaller imanager installer-name)]
    (try (.reloadBookList installer)
         (into () (.getBooks installer))
         (catch IllegalArgumentException exc
           (throw (AssertionError. "Invalid Installer"))))))

(defn- book-data
  "Converts SwordBook meta-data into a map."
  [swordbook]
  (let [data (.getBookMetaData (.getBook swordbook))]
    {:name (.getName data)
     :category (.getName (.getBookCategory data))
     :language (.getName (.getLanguage data))
     :versification (.getName (.getVersification swordbook))
     :supported (.isSupported data)
     :questionable? (.isQuestionable data)
     :osis-id (.getOsisID data)
     :encrypted? (.isEnciphered data)
     :locked? (.isLocked data)
     :library-uri (.getLibrary data)
     :location-uri (.getLocation data)
     :initials (.getInitials data)
     :book-charset (.getBookCharset data)
     :type (.toString (.getBookType data))
     :l-to-r? (.isLeftToRight data)
     }))

(defn- classify
  "Function for classifying a book."
  [book]
  (let [cbook (:category book)]
    (cond (= cbook "Biblical Texts") :bibles
          (= cbook "Commentaries") :commentaries
          (= cbook "Lexicons / Dictionaries") :dictionaries
          (= cbook "Glossaries") :glossaries
          (= cbook "Daily Devotional") :devotionals
          (= cbook "Generic Books") :generic-books
          (= cbook "Cults / Unorthodox / Questionable Material") :unorthodox
          :else :un-classified)))

(defn sorted-book-data-site
  "Returns book data from the given site."
  [site]
  (let [library (atom {})]
    (let [data (map book-data (get-available-books site))]
      (doseq [book data]
        (let [category (classify book)]
          (when-not (category @library)
            (swap! library assoc category []))
          (swap! library update-in [category] conj book)))
      @library)))

(defn- get-book
  "Retrieves the book object"
  [initials installer-name]
  (let [installer (.getInstaller imanager installer-name)
        bookfilter (reify BookFilter
                     (test [this bk] (= (.getInitials bk) initials)))]
    (try (.reloadBookList installer)
         (.get (.getBooks installer bookfilter) 0)
         (catch IllegalArgumentException exc
           (throw (AssertionError. "Invalid Installer"))))))

(defn install-book
  [book-initials site]
  (let [installer (.getInstaller imanager site)
        book (get-book book-initials site)]
    (.install installer book)))
