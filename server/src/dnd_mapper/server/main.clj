(ns dnd-mapper.server.main
  (:require
    [chord.http-kit :refer [wrap-websocket-handler]]
    [org.httpkit.server :refer [run-server]]
    [compojure.core :refer [defroutes GET ANY]]
    [mount.core :as mount :refer [defstate]]
    [dnd-mapper.server.messages :as messages]
    [cheshire.core :as json]
    [compojure.route :as route]
    [clojure.tools.logging :as log]
    [clojure.string :as string]
    [clojure.core.async :as a :refer [<! >! close! go]]))

(def port (Integer/parseInt (get (System/getenv) "PORT" "3000")))

(defonce ws-connections (atom {}))

(defroutes api
  (GET "/" []
       {:status 200
        :body (-> "public/index.html"
                  slurp)})

  (GET "/clients" []
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body (json/generate-string (->> (vals @ws-connections)
                                         (map #(dissoc % :ch)))
                                    {:key-fn #(-> % name (string/replace #"-" "_"))})})

  (GET "/keep-alive"
       []
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body (json/generate-string {:status :ok} "")})

  (ANY "/ws" {:keys [ws-channel] :as req}
       (a/go-loop
         [ws-ch ws-channel]
         (let [timeout-ch (a/timeout (* 1000 60 60 8))]
           (a/alt!
             timeout-ch (do
                          (log/info (format "Closing channel %s"
                                            (pr-str ws-ch)))
                          (swap! ws-connections dissoc ws-ch)
                          (close! ws-ch))
             ws-ch ([{:keys [message]}]
                    (when message
                      (let [{:keys [session-id host instance-id]} message]
                        (swap! ws-connections
                               assoc
                               ws-ch
                               {:session-id  session-id
                                :ch          ws-ch
                                :host        host
                                :instance-id instance-id})
                        (log/debug (format "Message received: %s"
                                           (pr-str message)))
                        (messages/process-message! message
                                                   ws-ch
                                                   @ws-connections)
                        ;(>! ws-ch "Hello client from server!")
                        (recur ws-ch)))))))
       {:status 200})
  (route/files "/"))

(defstate http-server
  :start (do (log/info (format "Starting web server on http://localhost:%d" port))
             (run-server (-> #'api wrap-websocket-handler)
                         {:port port}))
  :stop (do (log/info  "Stopping web server!")
            (http-server)))

(defn -main
  [& args]
  (mount/start))

(comment

  (clojure.pprint/pprint (vals @ws-connections))

  (do
    (mount/stop)
    (mount/start))

  )
