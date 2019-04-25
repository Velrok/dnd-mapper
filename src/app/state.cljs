(ns app.state
  (:require [reagent.core :as r]))

(defonce active-view-id (r/atom :start))

(defonce highlight-overlay (r/atom false))
(defonce map-width  (r/atom 35))
(defonce map-height (r/atom 50))
(defonce dm?        (r/atom false))
(defonce username   (r/atom nil))

(defonce fog-of-war-mode (r/atom :reveil)) ; :obscure

(defonce reveiled-cells (r/atom {}))

(defonce dnd-map
  (r/atom {:img-url "https://img00.deviantart.net/d36a/i/2015/115/3/0/abandoned_temple_of_blackfire_by_dlimedia-d4pponv.jpg"
           :alt "Created by DLIMedia: https://www.deviantart.com/dlimedia/art/Abandoned-Temple-of-Blackfire-285053467"}))

(defonce players
  (r/atom
    [(r/atom {:id (str (gensym "player"))
              :name "Negwen"
              :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/4729/162/150/300/636756769380492799.png"
              :player-visible true
              :on-map false
              :position nil
              :dead false})
     (r/atom {:id (str (gensym "player"))
              :name "Ikara"
              :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/17/747/150/150/636378331895705713.jpeg"
              :player-visible true
              :on-map false
              :position nil
              :dead false})
     (r/atom {:id (str (gensym "player"))
              :name "Udrik"
              :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"
              :player-visible true
              :on-map false
              :position nil
              :dead false})]))

(defn host-default-state!
  []
  (reset! dm? true)
  (reset! highlight-overlay true))

(defn guest-default-state!
  []
  (reset! dm? false)
  (reset! highlight-overlay false))

