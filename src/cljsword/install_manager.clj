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
  "Returns a map of known Crosswire installers."
  []
  (.getInstallers imanager))

(defn get-available-books
  "Returns a list of available books for a given installer."
  [installer-name]
  (let [installer (.getInstaller imanager installer-name)]
    (try (.reloadBookList installer)
         (.getBooks installer)
         (catch IllegalArgumentException exc
           (throw (AssertionError. "Invalid Installer"))))))

(defn book-data
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

(defn book-data-site
  "Returns book data from the given site."
  [site]
  (let [data (map book-data (get-available-books site))]
    {:Bibles
     (filterv #(= (:category %) "Biblical Texts") data)}))

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
