(ns app.main
  (:require [reagent.core :as r]))

(defn app
  []
  [:h1 "DND Mapper"])

(defn reload!
  []
  (prn ::reload!))

(defn ^:export  main!
  []
  (prn ::main!)
  (r/render [app]
            (js/document.getElementById "app")))
