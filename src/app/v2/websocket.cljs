(ns app.v2.websocket
  (:require
    [mount.core :as mount :refer [defstate]]
    [cljs.core.async :refer-macros [go]]))

(defstate websocket
  :start  (let [ws (js/WebSocket. (str "ws://localhost:3000/ws"))]
            (.addEventListener ws "close" #(prn ::close))
            (.addEventListener ws "error" #(prn ::error))
            (.addEventListener ws "message" #(tap> [::message (.-data %)]))
            (.addEventListener ws "open"  #(prn ::open))
            ws)
  :stop (.close @websocket 1000 "Closing session"))

(defn send!
  [message]
  (if (= (.-OPEN js/WebSocket)
         (.-readyState @websocket))
    (do (.send @websocket (pr-str message))
        ::send)
    ::not-send))

(defn ping!
  []
  (send! {:session-id  "asdfasdf"
          :host        true
          :instance-id "12341234"
          :audience    :server
          :data        {:type :heart-beat}
          :ts          (-> (js/Date.) (.getTime))} ))
