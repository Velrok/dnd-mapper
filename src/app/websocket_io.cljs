(ns app.websocket-io
  (:require [chord.client :refer [ws-ch]]
            [reagent.core :as r]
            [cljs.core.async :as a :refer [<! >! put! close!]]
            [cemerick.uri :refer [uri]]
            [mount.core :as mount]
            [app.browser :as browser :refer [pp]]
            [mount.core :refer-macros [defstate]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def endpoint
  (let [{host :host port :port proto :protocol}
        (-> js/window
            .-location
            .-href
            uri)]
    (str (if (= "https" proto)
           "wss://"
           "ws://")
         host
         (when (< 0 port)
           (str ":" port))
         "/ws")))

(defonce instance-id (-> (Math/random) (* 10000000) int str))
(defonce session-id (r/atom nil))
(defonce session-host (r/atom nil))

(defonce server-messages (r/atom nil))
(defonce session-ch (r/atom nil))

(defn join-or-create-session
  [session-id]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch endpoint))]
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
      (when (browser/debug?)
        (println (str "[" audience "]"
                      " > "
                      (prn-str msg))))
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
        ::channel (do
                    (reset! session-ch ch)
                    (reset! server-messages (a/mult ch))
                    ch)
        ::error   (pp message)))))

(defn create!
  []
  (reset! session-id (-> (Math/random)
                         (* 100000000)
                         int
                         str))
  (reset! session-host true)
  @heart-beat
  ;@state-broadcast
  (go
    (let [{:keys [type ch message]} (<! (join-or-create-session @session-id))]
      (case type
        ::channel (do
                    (reset! session-ch ch)
                    (reset! server-messages (a/mult ch))
                    ch)
        ::error   (pp message)))))


;(go
;  (let [{:keys [ws-channel]} (<! (ws-ch endpoint))
;        {:keys [message]} (<! ws-channel)]
;    (js/console.log "Got message from server:" (pr-str message))))

;(go
;  (let [{:keys [ws-channel]} (<! (ws-ch endpoint))
;        {:keys [message error]} (<! ws-channel)]
;    (if error
;      (js/console.log "Uh oh:" error)
;      (js/console.log "Hooray! Message:" (pr-str message)))))
