(defproject dnd-mapper "0.0.1-SNAPSHOT"
  :description "the server for dnd-mapper"
  :license {:name "Apache License 2.0"
            :url ""}

  :min-lein-version "2.7.1"

  :source-paths ["server/src"]

  :main dnd-mapper.server.main

  :dependencies
  [[org.clojure/clojure "1.10.0"]
   [org.clojure/core.async "0.4.490"]
   [compojure "1.6.1"]
   [mount "0.1.16"]
   [jarohen/chord "0.8.1"]])
