(ns app.main
  (:require
    [re-frame.core :as rf]
    [re-frame.db :refer [app-db]]
    [reagent.core :as r]
    [app.message-processing :as msg-process]
    [app.event-handlers]
    [app.subscriptions]
    [app.state :as state]
    [app.local-storage :as local-storage]
    [mount.core :as mount]
    [mount.core :refer-macros [defstate]]
    ;[app.views :as v]
    ;[app.browser :as browser]
    [app.websocket-io :as ws]
    [cljs.core.async :as a :refer [chan >! <! close!]]
    [cljs.core.async :refer-macros [go]]))

(add-watch app-db
           "persist-app-state"
           (fn [_key _ref old-v new-v]
             (some-> new-v
                     (select-keys [:dm? :session-id])
                     (local-storage/set! new-v))))

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


; View Functions

(defn app
  []
  (let [active-view @(rf/subscribe [:active-view])]
     [active-view]))

(defn ^:dev/after-load render
  []
  (r/render [app] (js/document.getElementById "app")))

(defn- start-heroku-keep-alive!
  []
  (js/window.setInterval
    #(js/fetch "/keep-alive") 5000))

(defn ^:export  main
  []
  (start-heroku-keep-alive!)
  (rf/dispatch-sync [:initialize])
  (ws/connect!)
  (render))
