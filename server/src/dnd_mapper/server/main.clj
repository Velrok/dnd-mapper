(ns dnd-mapper.server.main
  (:require
    [chord.http-kit :refer [wrap-websocket-handler]]
    [org.httpkit.server :refer [run-server]]
    [compojure.core :refer [defroutes GET ANY]]
    [compojure.route :as route]
    [clojure.core.async :as a :refer [<! >! close! go]]))

(def port (Integer/parseInt (get (System/getenv) "PORT" "3000")))

(defonce sessions (atom {:by-id {}
                         :by-ch {}}))

(defmulti process-message! (fn [msg & _others]
                            (:type msg)))

(defmethod process-message! :heart-beat
  [& _args]
  ::no-op)

(defmethod process-message! :state-broadcast
  [{:keys [state]} ch session-id sessions]
  (let [session-channels (->> sessions
                              :by-ch
                              (filter (fn [[k v]]
                                        (prn [::filter
                                              :kv [k v]])
                                        (= session-id v)))
                              (map first)
                              set)]
    (println (format "Broadcasting to %d channels."
                     (count session-channels)))
    (doseq [c (disj session-channels ch)]
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
                        (swap! sessions assoc-in
                               [:by-ch ws-ch]
                               session-id)
                        (println "Message received:" message)
                        (>! ws-ch "Hello client from server!")
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

(defn -main
  [& args]
  (println (format "Starting web server on http://localhost:%d" port))
  (run-server (-> #'api wrap-websocket-handler)
              {:port port}))

(comment
  
  (-main)
  
  )
