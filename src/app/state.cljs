(ns app.state
  (:require
    [app.websocket-io :as ws]
    [clojure.data :refer [diff]]
    [mount.core :refer-macros [defstate]]
    [reagent.core :as r]))

(def initial-app-value
  {:active-view-id :start
   :highlight-overlay true
   :map {:width 12
         :height 15
         :padding {:left 9 :right 11 :top 27 :bottom 57}
         :img-url "https://pre00.deviantart.net/dee0/th/pre/i/2015/116/0/b/the_desecrated_temple_by_theredepic-d4d4x56.jpg"
         :img-alt "Map art by JaredBlando https://www.deviantart.com/jaredblando"}
   :dm? true
   :fog-of-war-mode :reveil
   :reveiled-cells #{}
   :highlighted-cells #{}
   :session-id nil
   :players {"neg1"   {:id "neg1"
                       :initiative 10
                       :hp 100
                       :name "Negwen"
                       :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/4729/162/150/300/636756769380492799.png"
                       :player-visible true
                       :on-map false
                       :position nil
                       :dm-focus false
                       :dead false}
             "ikara1" {:id "ikara1"
                       :initiative 12
                       :hp 100
                       :name "Ikara"
                       :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/17/747/150/150/636378331895705713.jpeg"
                       :player-visible true
                       :on-map false
                       :position nil
                       :dm-focus false
                       :dead false}
             "Udrik"  {:id "Udrik"
                       :initiative 14
                       :hp 100
                       :name "Udrik"
                       :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"
                       :player-visible true
                       :on-map false
                       :position nil
                       :dm-focus false
                       :dead false}
             "goblin" {:id "goblin"
                       :initiative 8
                       :hp 7
                       :name "Goblin 1"
                       :img-url "https://i.imgur.com/kCysnYk.png"
                       :player-visible true
                       :on-map false
                       :position nil
                       :dm-focus false
                       :dead false}}})

(defn <loading>
  [props]
  [:p "Loading"])

(def current-view (r/atom <loading>))

(def state (r/atom initial-app-value))

(defstate report-state-diffs
  :start (do
           (.log js/console "START report-state-diffs")
           (add-watch state :differ
                    (fn [_key _atom old-state new-state]
                      (let [[strictly-old strictly-new _both] (diff old-state new-state)]
                        (.log js/console (clj->js ["strictly-old" strictly-old
                                                   "strictly-new" strictly-new]))
                        (ws/send! {:state-diff [strictly-old strictly-new]}
                                  {:session-id "asdf"})))))
  :stop (do
          (.log js/console "STOP report-state-diffs")
          (remove-watch state :differ)))

