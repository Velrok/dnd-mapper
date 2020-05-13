(ns dnd-mapper.server.main
  (:require
    ;[chord.http-kit :refer [wrap-websocket-handler]]
    [clojure.edn :as edn]
    [ring.middleware.params :refer [wrap-params]]
    [org.httpkit.server :refer [run-server with-channel] :as kit-s]
    [compojure.core :refer [defroutes GET ANY]]
    [mount.core :as mount :refer [defstate]]
    [dnd-mapper.server.messages :as messages]
    [cheshire.core :as json]
    [compojure.route :as route]
    [clojure.tools.logging :as log]
    ;[clojure.string :as string]
    ))

(def port (Integer/parseInt (get (System/getenv) "PORT" "3000")))

(defonce ws-connections (atom #{}))

(defroutes api
  (GET "/clients" []
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body (json/generate-string
                (map pr-str @ws-connections))})

  (GET "/keep-alive"
       []
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body (json/generate-string {:status :ok} "")})

  (ANY "/ws" req
       (with-channel req channel
         (kit-s/on-close channel
                         (fn [status]
                           (log/info "channel closed")))
         (if-not (kit-s/websocket? channel)
           {:status 400
            :body "Must be a websocket connection!"}
           (let [room (some-> req :params (get "room"))]
             (log/info "WebSocket channel")
             (kit-s/on-receive
               channel
               (fn [data] ; data received from client
                 (messages/process-message!
                  {:message (edn/read-string data)
                   :room room
                   :my-channel channel
                   :connections (->> @ws-connections
                                     (filter #(-> % :channel kit-s/open?))
                                     (remove #(= channel (-> % :channel)))
                                     (filter #(= room (some-> % :room))))
                   :send-fn (fn [{:keys [message channel]}]
                              (kit-s/send! channel (pr-str message)))})))
             (log/info "Tracking new channel " (pr-str channel))
             (swap! ws-connections conj {:room room
                                         :channel channel})))))

  (route/files "/assets")

  (GET "/*" []
       {:status 200
        :body (-> "public/index.html"
                  slurp)}))

(defstate http-server
  :start (do (log/info (format "Starting web server on http://localhost:%d" port))
             (run-server (-> #'api wrap-params)
                         {:port port}))
  :stop (do (log/info  "Stopping web server!")
            (http-server)))

(defn -main
  [& args]
  (mount/start))

(comment

  (clojure.pprint/pprint (vals @ws-connections))

  (assoc #{:a} :b)

  (do
    (mount/stop)
    (mount/start))

  )
