(ns dnd-mapper.server.main
  (:require
    [chord.http-kit :refer [wrap-websocket-handler]]
    [org.httpkit.server :refer [run-server]]
    [compojure.core :refer [defroutes GET ANY]]
    [mount.core :as mount :refer [defstate]]
    [cheshire.core :as json]
    [compojure.route :as route]
    [clojure.tools.logging :as log]
    [clojure.string :as string]
    [clojure.core.async :as a :refer [<! >! close! go]]))

(def port (Integer/parseInt (get (System/getenv) "PORT" "3000")))

(defonce ws-connections (atom {}))

(defn session-connections
  [s-id connections]
  (assert (map? connections) (str (type connections)))
  (assert (string? s-id) (str (type s-id)))
  (->> connections
       vals
       (filter #(= s-id (:session-id %)))
       set))

(defmulti process-message! (fn [msg _ch _connections]
                            (:audience msg)))

(defmethod process-message! :server
  [msg ch _connections]
  (go
    (let [now (System/currentTimeMillis)]
    (>! ch {:data [:heart-beat now (- now (:ts msg))]}))))

(defmethod process-message! :others
  [{:keys [session-id] :as msg} ch connections]
  (let [targets (disj (set (map :ch (session-connections session-id connections)))
                      ch)]
    (log/info (format "[%s] Forwarding messge to %d targets." session-id (count targets)))
    (doseq [c targets]
      (go (>! c msg)))))

(defmethod process-message! :guests
  [{:keys [session-id] :as msg} ch connections]
  (let [targets (disj (->> (session-connections session-id connections)
                           (filter #(false? (:host %)))
                           (map :ch)
                           set)
                      ch)]
    (log/info (format "[%s] Forwarding messge to %d guests." session-id (count targets)))
    (doseq [c targets]
      (go (>! c msg)))))

(defmethod process-message! :host
  [{:keys [session-id] :as msg} ch connections]
  (let [targets (disj (->> (session-connections session-id connections)
                           (filter #(true? (:host %)))
                           (map :ch)
                           set)
                      ch)]
    (log/info (format "[%s] Forwarding messge to %d host" session-id (count targets)))
    (doseq [c targets]
      (go (>! c msg)))))

(defmethod process-message! :default
  [message & _others]
  (log/warn (format "No handler for message audience %s" (:audience message))))

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
                          (log/info "Closing channel " ws-ch)
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
                        (log/debug "Message received:" message)
                        (process-message! message
                                          ws-ch
                                          @ws-connections)
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
