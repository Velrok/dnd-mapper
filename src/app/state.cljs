(ns app.state
  (:require [reagent.core :as r]))

(defonce active-view-id (r/atom :start))

(defonce highlight-overlay (r/atom false))
(defonce map-width  (r/atom 35))
(defonce map-height (r/atom 50))
(defonce dm?        (r/atom false))
(defonce username   (r/atom nil))

(defonce fog-of-war-mode (r/atom :reveil)) ; :obscure

(defonce reveiled-cells (r/atom #{}))

(defonce dnd-map
  (r/atom {:img-url "https://img00.deviantart.net/d36a/i/2015/115/3/0/abandoned_temple_of_blackfire_by_dlimedia-d4pponv.jpg"
           :alt "Created by DLIMedia: https://www.deviantart.com/dlimedia/art/Abandoned-Temple-of-Blackfire-285053467"}))
