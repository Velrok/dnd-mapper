(ns dnd-mapper.server.main
  (:require
    [chord.http-kit :refer [wrap-websocket-handler]]
    [org.httpkit.server :refer [run-server]]
    [compojure.core :refer [defroutes GET ANY]]
    [compojure.route :as route]
    [clojure.core.async :as a :refer [<! >! close! go]]))

(def port (Integer/parseInt (get (System/getenv) "PORT" "3000")))

(defroutes api
  (GET "/" []
       {:status 200
        :body (-> "public/index.html"
                  slurp)})
  (ANY "/ws" {:keys [ws-channel] :as req}
       (go
         (let [{:keys [message]} (<! ws-channel)]
           (println "Message received:" message)
           (>! ws-channel "Hello client from server!")
           (close! ws-channel)))
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
