(ns app.main
  (:require
    [reagent.core :as r]
    [app.routing :refer [router]]
    [app.message-processing :as msg-process]
    [app.browser :as browser]
    [mount.core :refer-macros [defstate]]
    [app.state :as state]
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

(defstate heroku-keep-alive
  :start (do
           (prn [::heroku-keep-alive "start"])
           (js/window.setInterval
             #(js/fetch "/keep-alive")
             10000))
  :stop  (do
           (prn [::heroku-keep-alive "stop"])
           (js/window.clearInterval @heroku-keep-alive)))


(defn app
  []
  [:<>
   [:h1 "hi"]
   [:button {:on-click #(browser/goto! "/" {:page 2})}
    "next page"]])

(defn <container>
  []
  [@state/current-view {}])

(defn render
  []
  (tap> [::render])
  (r/render
    [<container>]
    (js/document.getElementById "app")))

(defn ^:export main
  []
  (let [uri (browser/current-uri)]
    (browser/goto! (:path uri)
                   (:query uri)))
  (add-tap #(browser/log! "tap message" (clj->js %)))
  @router
  @ws/socket
  (render))
