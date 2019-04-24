(ns dnd-mapper.server.main
  (:require
    [chord.http-kit :refer [wrap-websocket-handler]]
    [org.httpkit.server :refer [run-server]]
    [compojure.core :refer [defroutes GET ANY]]
    [mount.core :as mount :refer [defstate]]
    [compojure.route :as route]
    [clojure.core.async :as a :refer [<! >! close! go]]))

(def port (Integer/parseInt (get (System/getenv) "PORT" "3000")))

(defonce sessions (atom {}))

(defmulti process-message! (fn [msg & _others]
                            (:type msg)))

(defmethod process-message! :heart-beat
  [_msg ch & _args]
  (go
    (>! ch "Ack.")))

(defmethod process-message! :state-broadcast
  [{:keys [state]} ch session-id sessions]
  (let [session-channels (->> sessions
                              (filter (fn [[_ch s-id]] (= session-id s-id)))
                              (map first)
                              set)
        targets (disj session-channels ch)]
    (doseq [c targets]
      (go (>! c {:type  :state-reset
                 :state state}))))
  ::no-op)

(defmethod process-message! :default
  [message & _others]
  (println (format "No handler for message of type %s" (:type message))))

(defroutes api
  (GET "/" []
       {:status 200
        :body (-> "public/index.html"
                  slurp)})
  (ANY "/ws" {:keys [ws-channel] :as req}
       (a/go-loop
         [ws-ch ws-channel]
         (let [timeout-ch (a/timeout (* 1000 60 10))]
           (a/alt!
             timeout-ch (do
                          (println "Closing channel " ws-ch)
                          (swap! sessions dissoc
                                 :by-ch
                                 ws-ch)
                          (close! ws-ch))
             ws-ch ([{:keys [message]}]
                    (when message
                      (let [{:keys [session-id]} message]
                        (swap! sessions
                               assoc ws-ch session-id)
                        (println "Message received:" message)
                        (process-message! (:message message)
                                          ws-ch
                                          session-id
                                          @sessions)
                        ;(>! ws-ch "Hello client from server!")
                        (recur ws-ch)))))))
       {:status 200})
  (route/files "/"))

;(defn handler [{:keys [ws-channel] :as req}]
;  (prn req)
;  (if ws-channel
;    (go
;      (let [{:keys [message]} (<! ws-channel)]
;        (println "Message received:" message)
;        (>! ws-channel "Hello client from server!")
;        (close! ws-channel)))
;    (case (:uri req)
;      "/" {:status 200
;           :body (-> "public/index.html"
;                     slurp)}
;      :else
;      {:status 404
;       :body "Not Found!"})
;    ))

(defstate http-server
  :start (do (println (format "Starting web server on http://localhost:%d" port))
             (run-server (-> #'api wrap-websocket-handler)
                         {:port port}))
  :stop (do (println  "Stopping web server!")
            (http-server)))

(defn -main
  [& args]
  (mount/start 'http-server)
  
  )

(comment

  (do
    (mount/stop)
    (mount/start))

  )
