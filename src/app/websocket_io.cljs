(ns app.websocket-io
  (:require [chord.client :refer [ws-ch]]
            [reagent.core :as r]
            [cljs.core.async :refer [<! >! put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def pp (.-log js/console))

(defonce session-id (r/atom nil))
(defonce session-ch (r/atom nil))

(defn join-or-create-session
  [session-id]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:3000/ws"))]
      (if-not error
        {:type ::channel
         :ch   ws-channel}
        (do (js/console.log "Error:" (pr-str error))
            {:type    ::error
             :message (pr-str error)})))))

(defn disconnect!
  []
  (swap! session-ch #(close! %)))

(defn create!
  []
  (reset! session-id (-> (Math/random)
                         (* 100000000)
                         int))
  (go
    (let [{:keys [type ch message]} (<! (join-or-create-session @session-id))]
      (case type
        ::channel (reset! session-ch ch)
        ::error   (pp message)))))

(defn send!
  [msg]
  (go
  (when @session-ch
    (>! @session-ch
        {:session-id @session-id
         :message msg
         :ts (-> (js/Date.) (.getTime))}))))

;(go
;  (let [{:keys [ws-channel]} (<! (ws-ch "ws://localhost:3000/ws"))
;        {:keys [message]} (<! ws-channel)]
;    (js/console.log "Got message from server:" (pr-str message))))

;(go
;  (let [{:keys [ws-channel]} (<! (ws-ch "ws://localhost:3000/ws"))
;        {:keys [message error]} (<! ws-channel)]
;    (if error
;      (js/console.log "Uh oh:" error)
;      (js/console.log "Hooray! Message:" (pr-str message)))))
