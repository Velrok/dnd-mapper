(ns app.websocket-io
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [<! >! put! close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn send-hello
  []
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch "ws://localhost:3000/ws"))]
      (if-not error
        (>! ws-channel "Hello server from client!")
        (js/console.log "Error:" (pr-str error))))))

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
