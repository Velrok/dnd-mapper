(ns app.main
  (:require [reagent.core :as r]))

(defonce active-view-id (r/atom ::home))

(defn <nav>
  []
  [:nav
   [:ul
    [:li {:on-click #(reset! active-view-id ::home)} "home"]
    [:li {:on-click #(reset! active-view-id ::session-new)} "new session"]
    [:li {:on-click #(reset! active-view-id ::session-join)} "join session"]]])

(defn <home>
  []
  [:div
   [:h1 "home view"]
   [:button {:on-click #(reset! active-view-id ::session-new)}
    "new session"]
   [:button "join session"]])



(defn <map-definition-input>
  [map-width map-height]
  (fn []
    [:div
     [:label {:for "#map-width"} "width"]
     [:input#map-width
      {:type :number
       :value @map-width
       :min 1
       :on-change #(do
                     (prn (some-> % .-target .-value int))
                     (reset! map-width (some-> % .-target .-value int)))
       }]

     [:label {:for "#map-height"} "height"]
     [:input#map-height
      {:type :number
       :min 1
       :value @map-height
       :on-change #(reset! map-height (some-> % .-target .-value int))
       }]]))

(defn <map-preview>
  [w h active-position]
  (fn []
    [:div
     [:img.map-preview__map {:src "https://img00.deviantart.net/d36a/i/2015/115/3/0/abandoned_temple_of_blackfire_by_dlimedia-d4pponv.jpg"
            :alt "Created by DLIMedia: https://www.deviantart.com/dlimedia/art/Abandoned-Temple-of-Blackfire-285053467"}]
     [:table.map-preview
      [:tbody.map-preview__tbody
       (doall
         (for [y (range @h)]
           [:tr {:key (str "m-prev-y" y)}
            (doall
              (for [x (range @w)]
                [:td.map-cell {:key (str "map-prev-yx-" y x)
                               :on-click #(reset! active-position [x y])
                               :class (if (= [x y] @active-position)
                                        "map-cell__active"
                                        "")}
                 (str "(" x "," y ")")]))]))]]]))

(defonce map-width  (r/atom 5))
(defonce map-height (r/atom 5))

(defn <session-new>
  []
  (let [active-position (r/atom nil)]
      [:div
       [:h2 "Sesson New "]
       [<map-definition-input> map-width map-height]

       [<map-preview> map-width map-height
        active-position]]))

(defn <session-join>
  []
  [:h2 "Sesson JOIN"])

(def views
  {::home         <home>
   ::session-new  <session-new>
   ::session-join <session-join>
   })

(defn app
  []
  [:div
   [:h1 "DND Mapper"]
   [<nav>]
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
