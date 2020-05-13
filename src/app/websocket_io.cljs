(ns app.websocket-io
  (:require
    [chord.client :refer [ws-ch]]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [cljs.core.async :as a :refer [<! >! put! close!]]
    [cemerick.uri :refer [uri]]
    [mount.core :as mount]
    [app.browser :as browser :refer [pp ]]
    [mount.core :refer-macros [defstate]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))


(def CONNECTING 0)
(def OPEN 1)
(def CLOSING 2)
(def CLOSED 3)

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

(declare state-change-tracker)

(defstate socket
  :start (let [_ (.log js/console "Connecting to " endpoint)
               s (new js/WebSocket (str endpoint "?room=" (browser/session-id)))]
           @state-change-tracker
           (set! (.-onopen    s) #(tap> [::open %]))
           (set! (.-onclose   s) #(tap> [::close % (.-wasClean %) (.-code %) (.-reason %)]))
           (set! (.-onmessage s) #(tap> [::message % (.-data %)]))
           (set! (.-onerror   s) #(tap> [::error  % (.-message %)]))
           (.log js/console "s" s)
           s)
  :stop (.close @socket))

(def ready-state (r/atom nil))

(defn handle-socket-msg
  [[event-type _]]
  (cond
    (= ::open event-type) (reset! ready-state (.-readyState @socket))
    (= ::close event-type) (reset! ready-state (.-readyState @socket))
    (= ::message event-type) (reset! ready-state (.-readyState @socket))
    (= ::error event-type) (reset! ready-state (.-readyState @socket))))

(def state-change-tracker
  (delay
    (add-tap handle-socket-msg)))

(defn open?
  [socket]
  (if socket
    (= (.-readyState socket)
       OPEN)
    false))

(defn ^:export send!
  "audience = #{:others :all :host :guests :server}"
  [msg {:keys [audience session-id host]
        :or {audience :others
             host false}}]
  (go
    (when (browser/debug?)
      (println (str (if host "!" ".")
                    (pr-str msg)
                    " -> "
                    "[" audience "]")))
    (when (open? @socket)
      (.send @socket
             (pr-str
               {:host        host
                :instance-id instance-id
                :audience    audience
                :data        msg
                :ts          (-> (js/Date.) (.getTime))})))))

(defn ping!
  []
  (send! "ping" {:audience :server}))


;(defn disconnect!
;  []
;  (mount/stop 'heart-beat 'state-broadcast)
;  (swap! session-ch #(close! %)))
