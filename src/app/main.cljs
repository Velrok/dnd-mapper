(ns app.main
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    [app.message-processing :as msg-process]
    [app.state :as state]
    [mount.core :as mount]
    [mount.core :refer-macros [defstate]]
    [app.views :as v]
    [app.browser :as browser]
    [app.websocket-io :as ws]
    [cljs.core.async :as a :refer [chan >! <! close!]]
    [cljs.core.async :refer-macros [go]]))

(defstate server-message-processor
  :start (do
           (prn [::server-message-processor "start"])
           (go
             (let [my-ch (a/chan)]
               (if-not @ws/server-messages
                 (prn [::ws/server-messages-empty!])
                 (do
                   (a/tap @ws/server-messages my-ch)
                   (go
                     (loop []
                       (if-let [msg (<! my-ch)]
                         (do (msg-process/process-server-message! msg)
                             (recur))
                         (do (prn [:empty-message])
                             (a/close! my-ch)))))))
               my-ch)))
  :stop  (do
           (prn [::server-message-processor "stop"])
           (a/close! @server-message-processor)))

(defstate ^{:on-reload :noop} create-session
  :start (let [cells-watcher-id (gensym "reveiled-cells-sync")
               players-w-id     (gensym "players-sync")
               map-img-w-id     (gensym "map-img-sync")
               map-width-w-id   (gensym "map-width-sync")
               map-height-w-id  (gensym "map-height-sync")]
           (go
             (let [server-messages (<! (ws/create!))]
               @server-message-processor))
           (add-watch state/reveiled-cells
                      cells-watcher-id
                      (fn [_k _a _old new-val]
                        (ws/send! {:type ::reveiled-cells-reset
                                   :data new-val}
                                  {:audience :guests})))
           
           (add-watch state/dnd-map
                      map-img-w-id
                      (fn [_k _a _old new-val]
                        (ws/send! {:type ::dnd-map-reset
                                   :data new-val}
                                  {:audience :guests})))
           (add-watch state/map-width
                      map-width-w-id
                      (fn [_k _a _old new-val]
                        (ws/send! {:type ::map-width-reset
                                   :data new-val}
                                  {:audience :guests})))
           (add-watch state/map-height
                      map-height-w-id
                      (fn [_k _a _old new-val]
                        (ws/send! {:type ::map-height-reset
                                   :data new-val}
                                  {:audience :guests})))
           {:watchers [[state/reveiled-cells cells-watcher-id]
                       [state/dnd-map        map-img-w-id]
                       [state/map-width      map-width-w-id]
                       [state/map-height     map-height-w-id]]})
  :stop (do
          (doseq [[a w-id] (:w-ids @create-session)]
            (remove-watch a w-id))))

(defstate ^{:on-reload :noop} join-session
  :start (do
           (go
             (let [_ (<! (ws/join! (get-in (browser/current-uri)
                                         [:query "join-session-id"])))]
               (ws/send! {:type ::request-state-init}
                         {})
               @server-message-processor))))

; Event dispatch

; Event Handlers

(rf/reg-event-db
  :initialize
  (fn [_db _event]
    {:active-view-id :start
     :highlight-overlay false
     :map {:width 35
           :height 50
           :img-url  "https://img00.deviantart.net/d36a/i/2015/115/3/0/abandoned_temple_of_blackfire_by_dlimedia-d4pponv.jpg"
           :img-alt  "Created by DLIMedia: https://www.deviantart.com/dlimedia/art/Abandoned-Temple-of-Blackfire-285053467"}
     :dm? false
     :fog-of-war-mode :reveil
     :reveiled-cells #{}
     :session-id nil
     :players {"neg1"   {:id "neg1"
                         :order 1
                         :name "Negwen"
                         :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/4729/162/150/300/636756769380492799.png"
                         :player-visible true
                         :on-map false
                         :position nil
                         :dead false}
               "ikara1" {:id "ikara1"
                         :order 2
                         :name "Ikara"
                         :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/17/747/150/150/636378331895705713.jpeg"
                         :player-visible true
                         :on-map false
                         :position nil
                         :dead false}
               "Udrik"  {:id "Udrik"
                         :order 3
                         :name "Udrik"
                         :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"
                         :player-visible true
                         :on-map false
                         :position nil
                         :dead false}}}))

(def views
  {:start        v/<start>
   :session-new  (partial v/<session-new>  {:state-init  create-session
                                            :session-id  ws/session-id})
   :session-join (partial v/<session-join> {:state-init  join-session
                                            :session-id  ws/session-id})})
(rf/reg-event-db
  :change-active-view
  (fn [db [_ view-id]]
    (assoc db :active-view-id view-id )))

(rf/reg-event-db
  :host-session
  (fn [db [_ session-id]]
    (-> db
        (assoc :session-id session-id)
        (assoc :dm? true)
        (assoc :active-view-id :session-new))))

(rf/reg-event-db
  :join-session
  (fn [db [_ session-id]]
    (-> db
        (assoc :session-id session-id)
        (assoc :dm? false)
        (assoc :active-view-id :session-join))))

; Query

(rf/reg-sub
  :active-view
  (fn [db _query-vec]
    (get views (:active-view-id db))))

(rf/reg-sub
  :token-count
  (fn [db _query-vec]
    (-> db :players count)))

(rf/reg-sub
  :tokens
  (fn [db _query-vec]
    (-> db :players)))

; View Functions



(defn app
  []
  [:div
   [:h1 "D&D Mapper"]
   (let [active-view @(rf/subscribe [:active-view])]
     [active-view])])


(defn ^:dev/after-load render
  []
  (mount/stop)
  (r/render [app] (js/document.getElementById "app")))

(defn ^:export  main
  []
  (rf/dispatch-sync [:initialize])
  (render))
