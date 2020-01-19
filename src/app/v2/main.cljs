(ns app.v2.main
  (:require
    [reagent.core :as r]
    ;; [app.message-processing :as msg-process]
    ;; [app.state :as state]
    ;; [app.local-storage :as local-storage]
    ;  [mount.core :as mount :refer [defstate]]
    ;; [mount.core :refer-macros [defstate]]
    ;; [app.views :as v]
    ;; [app.browser :as browser]
    ;; [app.websocket-io :as ws]
    [app.v2.websocket :as ws]
    [cljs.core.async :as a :refer [chan >! <! close!]]
    [cljs.core.async :refer-macros [go]]))

(defn logger
  [m]
  (.log js/console (js/Date.) m (pr-str m)))

(defonce taps
  (do (add-tap logger)))

; View Functions

(defn app
  []
  [:div
   [:h1 "D&D Mapper"]
   [:button
    {:on-click #(prn (ws/ping!))}
    "ping!"]
   ])


(defn ^:dev/after-load render
  []
  (r/render [app] (js/document.getElementById "app")))

(defn ^:export  main
  []
  ;@ws/websocket
  (render))

