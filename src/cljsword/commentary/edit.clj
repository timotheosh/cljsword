(ns cljsword.commentary.edit
  (:import
   [java.io File]
   [java.net URI]
   [org.crosswire.common.util
    IniSection
    IOUtil]
   [org.crosswire.jsword.versification.system Versifications]
   [org.crosswire.jsword.versification
    Versification
    BibleBook]
   [org.crosswire.jsword.book BookMetaData]
   [org.crosswire.jsword.book.sword
    RawFileBackend
    SwordBookMetaData
    SwordBookPath]
   [org.crosswire.jsword.passage Verse]
   ))

(defn create-commentary-configfile
  "Create an editable commentary module."
  [name description versification]
  ;; Create the config file.
  (let [v11n (.getVersification (Versifications/instance) versification)
        table (new IniSection name)
        config-file (new File
                         (str
                          (.toString (first (SwordBookPath/getSwordPath)))
                          "/mods.d/" name ".conf"))]
    (.add table (BookMetaData/KEY_LANG) "en")
    (.add table (SwordBookMetaData/KEY_DESCRIPTION) description)
    (.add table (SwordBookMetaData/KEY_MOD_DRV) "RawFiles")
    (.add table (SwordBookMetaData/KEY_DATA_PATH)
          (str "./modules/comments/rawfiles/" name "/"))
    (.add table (SwordBookMetaData/KEY_ENCODING) "UTF-8")
    (.save table config-file "UTF-8")))

(defn add-commentary
  [commentary reference note]
  (let [book (cljsword.core/getBook commentary)
        swordBookMetaData (.getBookMetaData book)
        versification (.getProperty swordBookMetaData BookMetaData/KEY_VERSIFICATION)
        v11n (.getVersification (Versifications/instance) versification)
        backend (new RawFileBackend swordBookMetaData 2)
        state (.initState backend)
        verse (.getKey book reference)
        data (.getRawText backend state verse)]
    (.setRawText
     backend state verse
     (str data (when (not (empty? data)) "<br/><br/>") note))
    (IOUtil/close state)))

(defn clear-commentary
  [commentary reference]
  (let [book (cljsword.core/getBook commentary)
        swordBookMetaData (.getBookMetaData book)
        versification (.getProperty swordBookMetaData BookMetaData/KEY_VERSIFICATION)
        v11n (.getVersification (Versifications/instance) versification)
        backend (new RawFileBackend swordBookMetaData 2)
        state (.initState backend)
        verse (.getKey book reference)]
    (.setRawText backend state verse "")
    (IOUtil/close state)))
