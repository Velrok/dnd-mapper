(ns app.state
  (:require
    [app.browser :as browser]
    [app.local-storage :as local-storage]
    [app.websocket-io :as ws]
    [clojure.data :refer [diff]]
    [mount.core :refer-macros [defstate]]
    [reagent.core :as r]))

(defn <loading>
  [props]
  [:p "Loading"])

(def current-view (r/atom <loading>))

(def local
  (r/atom
    {:highlight-overlay true
     :dm?               false
     :fog-of-war-mode   :reveil}))

(def shared
  (r/atom
    {:map {:columns 37
           :rows 37
           :img-url "https://i.imgur.com/lb5jQr6.jpg"
           :reveiled-cells #{}
           :highlighted-cells #{}}
     :tokens {"ikara1" {:id "ikara1"
                        :initiative 10
                        :size :medium
                        :position {:row 0 :column 1}
                        :hp 100
                        :max-hp 100
                        :name "Ikara"
                        :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/17/747/150/150/636378331895705713.jpeg"
                        :player-visible true }
              "Udrik"  {:id "Udrik"
                        :initiative 12
                        :size :medium
                        :position {:row 0 :column 3}
                        :hp 100
                        :max-hp 100
                        :name "Udrik"
                        :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"
                        :player-visible true }
              "goblin" {:id "goblin"
                        :initiative 8
                        :size :small
                        :position {:row 0 :column 5}
                        :hp 7
                        :max-hp 7
                        :name "Goblin 1"
                        :img-url "https://i.imgur.com/kCysnYk.png"
                        :player-visible true }
              "imp" {:id "imp"
                     :initiative 6
                     :size :tiny
                     :position {:row 1 :column 1}
                     :hp 7
                     :max-hp 7
                     :name "Imp"
                     :img-url  "https://media-waterdeep.cursecdn.com/avatars/0/360/636252778559844878.jpeg"
                     :player-visible true }
              "orc"{:id "orc"
                    :initiative 6
                    :size :medium
                    :position {:row 2 :column 2}
                    :hp 7
                    :max-hp 7
                    :name "Orc"
                    :img-url  "https://media-waterdeep.cursecdn.com/avatars/0/300/636252771690760688.jpeg"
                    :player-visible true }
              "ogre" {:id "ogre"
                      :initiative 6
                      :size :large
                      :position {:row 2 :column 6}
                      :hp 7
                      :max-hp 7
                      :name "Ogre"
                      :img-url  ""
                      :player-visible true }
              "treant" {:id "treant"
                        :initiative 6
                        :size :huge
                        :position {:row 5 :column 1}
                        :hp 7
                        :max-hp 7
                        :name "Treant"
                        :img-url  "https://media-waterdeep.cursecdn.com/avatars/0/96/636252740537360603.jpeg"
                        :player-visible true }
              "worm" {:id "worm"
                      :initiative 6
                      :size :gargantuan
                      :position {:row 5 :column 8}
                      :hp 7
                      :max-hp 7
                      :name "Purple Worm"
                      :img-url  "https://media-waterdeep.cursecdn.com/avatars/0/274/636252769845811638.jpeg"
                      :player-visible true }
              }}))

(defstate report-state-diffs
  :start (do
           (.log js/console "START report-state-diffs")
           (add-watch shared :differ
                      (fn [_key _atom old-state new-state]
                        (when-let [s-id (browser/session-id)]
                          (let [[strictly-old strictly-new _both] (diff old-state new-state)]
                            (.log js/console (clj->js ["strictly-old" strictly-old
                                                       "strictly-new" strictly-new]))
                            (ws/send! {:state-diff [strictly-old strictly-new]}
                                      {:session-id s-id}))))))
  :stop (do
          (.log js/console "STOP report-state-diffs")
          (remove-watch shared :differ)))

(defstate persist-state-changes
  :start (do
           (.log js/console "START persist-state-changes")
           (add-watch shared :persist-state-changes
                      (fn [_key _atom old-state new-state]
                        (prn "persist-state-changes>>>>>>>>>>")
                        (browser/log! new-state)
                        (when-let [s-id (browser/session-id)]
                          (browser/log! {:dm? (:dm? @local)
                                         :session-id s-id})
                          (local-storage/set! {:dm? (:dm? @local)
                                               :session-id s-id}
                                              new-state)))))
  :stop (do
          (.log js/console "STOP persist-state-changes")
          (remove-watch shared :persist-state-changes)))
