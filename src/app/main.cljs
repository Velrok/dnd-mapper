(ns app.main
  (:require
    [reagent.core :as r]
    [app.state :as state]
    [mount.core :as mount]
    [mount.core :refer-macros [defstate]]
    [app.views :as v]
    [app.browser :as browser]
    [app.websocket-io :as ws]
    [cljs.core.async :as a :refer [chan >! <! close!]]
    [cljs.core.async :refer-macros [go]]))

(defmulti process-server-message!
  (fn [{:keys [message]}]
    (-> message :data :type)))

(defmethod process-server-message! :default
  [{:keys [message]}]
  (prn [:no-handler-for message]))

(defmethod process-server-message! ::reveiled-cells-reset
  [{:keys [message]}]
  (reset! state/reveiled-cells
          (some-> message :data :data)))

(defmethod process-server-message! ::players-reset
  [{:keys [message]}]
  (reset! state/players
          (some-> message :data :data)))

(defmethod process-server-message! ::dnd-map-reset
  [{:keys [message]}]
  (reset! state/dnd-map
          (some-> message :data :data)))

(defmethod process-server-message! ::map-width-reset
  [{:keys [message]}]
  (reset! state/map-width
          (some-> message :data :data)))

(defmethod process-server-message! ::map-height-reset
  [{:keys [message]}]
  (reset! state/map-height
          (some-> message :data :data)))

(defmethod process-server-message! ::request-state-init
  [_message]
  (prn [::process-server-message!-request-state-init])
  (when @state/dm?
    (doseq [m [{:type ::reveiled-cells-reset :data @state/reveiled-cells}
               {:type ::players-reset        :data @state/players}
               {:type ::dnd-map-reset        :data @state/dnd-map}
               {:type ::map-width-reset      :data @state/map-width}
               {:type ::map-height-reset     :data @state/map-height}]]
      (ws/send! m {:audience :guests}))))

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
                         (do (process-server-message! msg)
                             (recur))
                         (do (prn [:empty-message])
                             (a/close! my-ch)))))))
               my-ch)))
  :stop  (do
           (prn [::server-message-processor "stop"])
           (a/close! @server-message-processor)))

(defstate create-session
  :start (let [cells-watcher-id (gensym "reveiled-cells-sync")
               players-w-id     (gensym "players-sync")
               map-img-w-id     (gensym "map-img-sync")
               map-width-w-id   (gensym "map-width-sync")
               map-height-w-id  (gensym "map-height-sync")]
           (state/host-default-state!)
           (go
             (let [server-messages (<! (ws/create!))]
               @server-message-processor))
           (add-watch state/reveiled-cells
                      cells-watcher-id
                      (fn [_k _a _old new-val]
                        (ws/send! {:type ::reveiled-cells-reset
                                   :data new-val}
                                  {:audience :guests})))
           (add-watch state/players
                      players-w-id
                      (fn [_k _a _old new-val]
                        (ws/send! {:type ::players-reset
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
                       [state/players        players-w-id]
                       [state/dnd-map        map-img-w-id]
                       [state/map-width      map-width-w-id]
                       [state/map-height     map-height-w-id]]})
  :stop (do
          (doseq [[a w-id] (:w-ids @create-session)]
            (remove-watch a w-id))))

(defstate join-session
  :start (do
           (state/guest-default-state!)
           (go
             (let [_ (<! (ws/join! (get-in (browser/current-uri)
                                         [:query "join-session-id"])))]
               (ws/send! {:type ::request-state-init}
                         {})
               @server-message-processor))))

(def views
  {:start        v/<start>
   :session-new  (partial v/<session-new>  {:state-init  create-session
                                            :session-id  ws/session-id})
   :session-join (partial v/<session-join> {:state-init  join-session
                                            :session-id  ws/session-id})})

(defn app
  []
  [:div
   [:h1 "D&D Mapper"]
   ;[<nav>]
   (let [active-view (get views @state/active-view-id)]
     [active-view])])


(defn ^:dev/after-load render
  []
  (mount/stop)
  (r/render [app] (js/document.getElementById "app")))

(defn ^:export  main
  []
  (render))
