(ns app.websocket-io
  (:require
    [chord.client :refer [ws-ch]]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [cljs.core.async :as a :refer [<! >! put! close!]]
    [cemerick.uri :refer [uri]]
    [mount.core :as mount]
    [app.browser :as browser :refer [pp]]
    [mount.core :refer-macros [defstate]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

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
  [msg {:keys [audience session-id host]
        :or {audience :others
             host false}}]
  (go
    (when (empty? session-id)
      (.error js/console "called send! without session-id"))
    (when (browser/debug?)
      (println (str (if host "!" ".")
                    (pr-str msg)
                    " -> "
                    "[" audience "]")))
    (when @session-ch
      (>! @session-ch
          {:session-id  (str session-id)
           :host        host
           :instance-id instance-id
           :audience    audience
           :data        msg
           :ts          (-> (js/Date.) (.getTime))}))))

(defstate ^{:on-reload :noop} server-message-dispatch
  :start
  (do
    (prn "starting: server msg dispatch")
    (go-loop
      []
      (if-not @session-ch
        (do
          (prn "session-ch nil waiting for retry ...")
          (a/<! (a/timeout 1000))
          (recur))
        (do
          (prn "session-ch established listening ...")
          (go-loop
            []
            (when-let [server-event (a/<! @session-ch)]
              (when (browser/debug?)
                (println (str " <- " (pr-str server-event))))
              (rf/dispatch (-> server-event :message :data))
              (recur))))))))


(defn connect!
  []
  (prn ::connect!)
  @connection
  @server-message-dispatch)


;(defn disconnect!
;  []
;  (mount/stop 'heart-beat 'state-broadcast)
;  (swap! session-ch #(close! %)))
