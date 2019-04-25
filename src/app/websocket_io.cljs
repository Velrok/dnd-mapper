(ns app.websocket-io
  (:require [chord.client :refer [ws-ch]]
            [reagent.core :as r]
            [cljs.core.async :refer [<! >! put! close!]]
            [mount.core :as mount]
            [mount.core :refer-macros [defstate]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def pp (.-log js/console))

(defonce instance-id (-> (Math/random) (* 10000000) int str))
(defonce session-id (r/atom nil))
(defonce session-ch (r/atom nil))
(defonce session-host (r/atom nil))

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

;; audience = #{:others :all :host :guests :server}
(defn send!
  [msg {:keys [audience]
        :or {audience :others}}]
  (go
    (when @session-ch
      (>! @session-ch
          {:session-id  (str @session-id)
           :host        @session-host
           :instance-id instance-id
           :audience    audience
           :data        msg
           :ts          (-> (js/Date.) (.getTime))}))))

(defstate heart-beat
  :start (js/window.setInterval #(send! {:type :heart-beat}
                                        {:audience :server})
                                5000)
  :stop (js/window.clearInterval @heart-beat))

(defn disconnect!
  []
  (mount/stop 'heart-beat 'state-broadcast)
  (swap! session-ch #(close! %)))

(defn join!
  [s-id]
  (reset! session-id (str s-id))
  (reset! session-host false)
  @heart-beat
  (go
    (let [{:keys [type ch message]} (<! (join-or-create-session @session-id))]
      (case type
        ::channel (reset! session-ch ch)
        ::error   (pp message)))))

(defonce distributed-state (atom {}))

(add-watch distributed-state
           :websocket-sync
           (fn [_key _atom old-val new-val]
             (when (and @session-host @session-ch)
               (send! {:type ::state-reset
                       :state new-val}
                      {:audience :guests}))))
(defstate state-broadcast
  :start (js/window.setInterval #(send!
                                   {:type  ::state-reset
                                    :state @distributed-state}
                                   {:audience :guests})
                                10000)
  :stop (js/window.clearInterval @state-broadcast))

(defn create!
  []
  (reset! session-id (-> (Math/random)
                         (* 100000000)
                         int
                         str))
  (reset! session-host true)
  @heart-beat
  @state-broadcast
  (go
    (let [{:keys [type ch message]} (<! (join-or-create-session @session-id))]
      (case type
        ::channel (reset! session-ch ch)
        ::error   (pp message)))))


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
