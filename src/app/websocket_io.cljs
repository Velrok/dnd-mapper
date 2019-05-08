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

(defstate ^{:on-reload :noop} connection
  :start
  (do
    (prn "starting: connection")
    (go
      (let [{:keys [ws-channel error]} (<! (ws-ch endpoint))]
        (if-not error
          (reset! session-ch ws-channel)
          (do
            (js/console.log "Error:" (pr-str error))
            error)))))

  :stop
  (do
    (prn "stopping: connection")
    (when @session-ch
      (go
        (a/close! @session-ch)))))

(defn ^:export send!
  "audience = #{:others :all :host :guests :server}"
  [msg {:keys [audience]
        :or {audience :others}}]
  (go
    (when (browser/debug?)
      (println (str (pr-str msg)
                    " -> "
                    "[" audience "]")))
    (when @session-ch
      (>! @session-ch
          {:session-id  (str @session-id)
           :host        @session-host
           :instance-id instance-id
           :audience    audience
           :data        msg
           :ts          (-> (js/Date.) (.getTime))}))))

(defstate heart-beat
  :start (do
           (prn (str "starting: heart-beat"))
           (js/window.setInterval #(send! {:type :heart-beat}
                                        {:audience :server})
                                5000))
  :stop (do
          (prn (str "stopping: heart-beat"))
          (js/window.clearInterval @heart-beat)))


(defn connect!
  []
  (prn ::connect!)
  @connection
  @heart-beat)


;(defn disconnect!
;  []
;  (mount/stop 'heart-beat 'state-broadcast)
;  (swap! session-ch #(close! %)))

(defn join-session!
  [s-id]
  (reset! session-id (str s-id))
  (reset! session-host false))

(defn create-session!
  []
  (reset! session-id (-> (Math/random)
                         (* 100000000)
                         int
                         str))
  (reset! session-host true))


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
