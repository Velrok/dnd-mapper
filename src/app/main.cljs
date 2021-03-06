(ns app.main
  (:require
    [re-frame.core :as rf]
    [re-frame.db :refer [app-db]]
    [reagent.core :as r]
    [app.event-handlers] ;; load for side effects of registering handlers
    [app.subscriptions]
    [app.message-processing :as msg-process]
    [app.local-storage :as local-storage]
    [mount.core :refer-macros [defstate]]
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

(defstate heroku-keep-alive
  :start (do
           (prn [::heroku-keep-alive "start"])
           (js/window.setInterval
             #(js/fetch "/keep-alive")
             10000))
  :stop  (do
           (prn [::heroku-keep-alive "stop"])
           (js/window.clearInterval @heroku-keep-alive)))

(defstate ticker
  :start (do
           (prn [::ticker "start"])
           (js/window.setInterval
            #(rf/dispatch [:now-ts-ms (.now js/Date)])
            1000))
  :stop  (do
           (prn [::ticker "stop"])
           (js/window.clearInterval @ticker)))


(defn app
  []
  (let [active-view @(rf/subscribe [:active-view])]
     [active-view]))

(defn ^:dev/after-load render
  []
  (r/render [app] (js/document.getElementById "app")))

(defn ^:export  main
  []
  @ticker
  @heroku-keep-alive
  @server-message-processor
  (rf/dispatch-sync [:initialize])
  (ws/connect!)
  (render))
