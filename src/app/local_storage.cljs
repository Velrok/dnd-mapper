(ns app.local-storage
  (:refer-clojure :exclude [get remove keys])
  (:require [clojure.edn :as edn]))

(def local-storage js/localStorage)

(defn remove! [k]
  (-> local-storage
      (.getItem "__keys__")
      edn/read-string
      (disj k)
      (pr-str)
      (.setItem "__keys__"))
  (-> local-storage
      (.removeItem k)))

(defn get [k]
  (-> local-storage
      (.getItem (pr-str k))
      edn/read-string))

(defn set! [k v]
  (.setItem local-storage
            "__keys__"
            (-> local-storage
                (.getItem  "__keys__")
                edn/read-string
                set
                (conj k)
                (pr-str)))
  (.setItem local-storage (pr-str k) (pr-str v)))

(defn keys []
  (or
    (edn/read-string
      (.getItem local-storage "__keys__" ))
    #{}))
