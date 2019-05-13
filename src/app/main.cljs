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
     :dm? true
     :fog-of-war-mode :reveil
     :reveiled-cells #{}
     :highlighted-cells #{}
     :session-id nil
     :players {"neg1"   {:id "neg1"
                         :order 1
                         :name "Negwen"
                         :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/4729/162/150/300/636756769380492799.png"
                         :player-visible true
                         :on-map false
                         :position nil
                         :dm-focus false
                         :dead false}
               "ikara1" {:id "ikara1"
                         :order 2
                         :name "Ikara"
                         :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/17/747/150/150/636378331895705713.jpeg"
                         :player-visible true
                         :on-map false
                         :position nil
                         :dm-focus false
                         :dead false}
               "Udrik"  {:id "Udrik"
                         :order 3
                         :name "Udrik"
                         :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"
                         :player-visible true
                         :on-map false
                         :position nil
                         :dm-focus false
                         :dead false}}}))

(def broadcast-if-host
  (rf/->interceptor
    :id      :broadcast-if-host
    :after   (fn [context]
               (let [db (some-> context :coeffects :db)]
                 (when (:dm? db)
                   (if-let [e (some-> context :coeffects :event)]
                     (ws/send! e {:audience :guests
                                  :host true
                                  :session-id (:session-id db)}))))
               context)))

(def broadcast-if-guest
  (rf/->interceptor
    :id      :broadcast-if-guest
    :after   (fn [context]
               (let [db (some-> context :coeffects :db)]
                 (when-not (:dm? db)
                   (if-let [e (some-> context :coeffects :event)]
                     (ws/send! e {:audience :host
                                  :host false
                                  :session-id (:session-id db)}))))
               context)))

(def views
  {:start        v/<start>
   :session-new  v/<session-new>
   :session-join v/<session-join>})

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

(rf/reg-event-fx
  :join-session
  (fn [{:keys [db]} [_ session-id]]
    {:db (-> db
             (assoc :session-id session-id)
             (assoc :dm? false)
             (assoc :active-view-id :session-join))
     :dispatch [:request-state-init]}))

(rf/reg-event-fx
  :request-state-init
  [broadcast-if-guest]
  (fn [context event]
    (if (-> context :db :dm?)
      {:dispatch [:state-init (-> context
                                  :db
                                  (select-keys [:map :reveiled-cells :players]))]})))

(rf/reg-event-db
  :state-init
  [broadcast-if-host]
  (fn [db [_ dm-state]]
    (merge db dm-state)))

(rf/reg-event-db
  :highlight-overlay-changed
  (fn [db [_ value]]
    (-> db (assoc :highlight-overlay value))))

(rf/reg-event-db
  :map-img-url-changed
  [broadcast-if-host]
  (fn [db [_ img-url]]
    (-> db
        (assoc-in [:map :img-url] img-url)
        (assoc-in [:map :img-alt] ""))))

(rf/reg-event-db
  :map-width-changed
  [broadcast-if-host]
  (fn [db [_ w]]
    (-> db
        (assoc-in [:map :width] w))))

(rf/reg-event-db
  :map-height-changed
  [broadcast-if-host]
  (fn [db [_ h]]
    (-> db
        (assoc-in [:map :height] h))))

(rf/reg-event-db
  :token-gain-dm-focus
  (fn [db [_ token-id]]
    (-> db
        (assoc-in [:players token-id :dm-focus] true))))

(rf/reg-event-db
  :token-loose-dm-focus
  (fn [db [_ token-id]]
    (-> db
        (assoc-in [:players token-id :dm-focus] false))))

(rf/reg-event-db
  :token-position-change
  [broadcast-if-host]
  (fn [db [_ token-id position]]
    (-> db (assoc-in [:players token-id :position] position))))

(rf/reg-event-db
  :token-name-change
  [broadcast-if-host]
  (fn [db [_ token-id name]]
    (-> db (assoc-in [:players token-id :name] name))))

(rf/reg-event-db
  :reveil-cells
  [broadcast-if-host]
  (fn [db [_ cells]]
    (-> db
        (update-in [:reveiled-cells] #(into % cells)))))

(rf/reg-event-db
  :obscure-cells
  [broadcast-if-host]
  (fn [db [_ cells]]
    (-> db
        (update-in [:reveiled-cells] #(apply disj % cells)))))

(rf/reg-event-db
  :token-dead-change
  [broadcast-if-host]
  (fn [db [_ token-id dead?]]
    (-> db (assoc-in [:players token-id :dead] dead?))))

(rf/reg-event-db
  :token-visible-change
  [broadcast-if-host]
  (fn [db [_ token-id visible?]]
    (-> db (assoc-in [:players token-id :player-visible] visible?))))

(rf/reg-event-db
  :token-img-url-change
  [broadcast-if-host]
  (fn [db [_ token-id img-url]]
    (-> db (assoc-in [:players token-id :img-url] img-url))))

(rf/reg-event-db
  :add-token
  [broadcast-if-host]
  (fn [db [_ token]]
    (-> db (assoc-in [:players (:id token)] token))))

(rf/reg-event-db
  :delete-token
  [broadcast-if-host]
  (fn [db [_ token-id]]
    (-> db (update-in [:players] dissoc token-id))))

(rf/reg-event-db
  :set-fog-of-war-mode
  (fn [db [_ mode]]
    (-> db (assoc-in [:fog-of-war-mode] mode))))

(rf/reg-event-db
  :heart-beat
  (fn [db [_ ts latency]]
    (-> db (assoc-in [:last-heart-beat] (js/Date. ts)))))

(rf/reg-event-fx
  :request-cell-highlight
  [broadcast-if-guest]
  (fn [{:keys [db] :as context} [_ pos origin-instance-id]]
    (if (-> db :dm?)
        (do
          (js/window.setTimeout #(rf/dispatch [:turnoff-cell-highlight pos]) 2000)
          {:db (update-in db [:highlighted-cells] conj pos)})
        {})))

(rf/reg-event-db
  :turnoff-cell-highlight
  (fn [db [_ pos]]
    (update-in db [:highlighted-cells] disj pos)))

; Query

(rf/reg-sub
  :active-view
  (fn [db _query-vec]
    (get views (:active-view-id db))))

(rf/reg-sub
  :session-id
  (fn [db _query-vec]
    (-> db :session-id)))

(rf/reg-sub
  :token-count
  (fn [db _query-vec]
    (-> db :players count)))

(rf/reg-sub
  :tokens
  (fn [db _query-vec]
    (-> db :players)))

(rf/reg-sub
  :highlight-overlay
  (fn [db _query-vec]
    (-> db :highlight-overlay)))

(rf/reg-sub
  :map-width
  (fn [db _query-vec]
    (some-> db :map :width)))

(rf/reg-sub
  :map-height
  (fn [db _query-vec]
    (some-> db :map :height)))

(rf/reg-sub
  :map-img-url
  (fn [db _query-vec]
    (some-> db :map :img-url)))

(rf/reg-sub
  :map-img-alt
  (fn [db _query-vec]
    (some-> db :map :img-alt)))

(rf/reg-sub
  :dm?
  (fn [db _query-vec]
    (some-> db :dm?)))

(rf/reg-sub
  :reveiled-cells
  (fn [db _query-vec]
    (some-> db :reveiled-cells)))

(rf/reg-sub
  :fog-of-war-mode
  (fn [db _query-vec]
    (some-> db :fog-of-war-mode)))


(rf/reg-sub
  :highlighted-cells
  (fn [db _query-vec]
    (some-> db :highlighted-cells)))


; View Functions

(defn app
  []
  [:div
   [:h1 "D&D Mapper"]
   (let [active-view @(rf/subscribe [:active-view])]
     [active-view])])


(defn ^:dev/after-load render
  []
  (r/render [app] (js/document.getElementById "app")))

(defn ^:export  main
  []
  (rf/dispatch-sync [:initialize])
  (ws/connect!)
  (render))
