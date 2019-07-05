(ns app.local-storage
  (:refer-clojure :exclude [get remove keys])
  (:require [clojure.edn :as edn]
            ["lz-string" :as LZString]))

(def local-storage js/localStorage)

(defn- encode [x]
  (some->> x pr-str (.compress LZString)))

(defn- decode [x]
  (some->> x (.decompress LZString) edn/read-string))

(defn remove! [k]
  (.setItem local-storage
            "__keys__"
            (-> local-storage
                (.getItem "__keys__")
                decode
                set
                (disj k)
                (encode)))
  (-> local-storage (.removeItem k)))

(defn get [k]
  (-> local-storage
      (.getItem (encode k))
      decode))

(defn set! [k v]
  (.setItem local-storage
            "__keys__"
            (-> local-storage
                (.getItem  "__keys__")
                decode
                set
                (conj k)
                (encode)))
  (.setItem local-storage (encode k) (encode v)))

(defn keys []
  (or
    (decode
      (.getItem local-storage "__keys__" ))
    #{}))
