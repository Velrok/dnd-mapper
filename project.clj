(defproject dnd-mapper "0.0.1-SNAPSHOT"
  :description "the server for dnd-mapper"
  :license {:name "Apache License 2.0"
            :url ""}

  :min-lein-version "2.7.1"

  :source-paths ["server/src"]

  :main dnd-mapper.server.main

  :dependencies
  [[org.clojure/clojure "1.10.0"]
   [javax.xml.bind/jaxb-api "2.4.0-b180830.0359"]
   [org.clojure/tools.logging "0.4.1"]
   [org.clojure/core.async "0.4.490"]
   [compojure "1.6.1"]
   [com.novemberain/monger "3.1.0"]
   [mount "0.1.16"]
   [jarohen/chord "0.8.1"]])
