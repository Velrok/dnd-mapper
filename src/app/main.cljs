(ns app.main
  (:require [reagent.core :as r]))

(defonce active-view-id (r/atom :home))

(defn <home>
  []
  [:div
   [:h1 "home view"]
   [:button {:on-click #(do (prn ::new-session)
                            (reset! active-view-id :session-new))}
    "new session"]
   [:button "join session"]])

(defn <session-new>
  []
  [:h2 "Sesson New"])

(def views {:home        <home>
            :session-new <session-new>})

(defn app
  []
  [:div
   [:h1 "DND Mapper"]
   (let [active-view (get views @active-view-id)]
     [active-view])])

(defn ^:dev/after-load render
  []
  (prn ::render)
  (r/render [app] (js/document.getElementById "app")))

(defn ^:export  main
  []
  (prn ::main)
  (render))
