(ns dnd-mapper.server.messages
  (:require
    [clojure.tools.logging :as log]))

(defmulti process-message!
  (fn [{:keys [message]}]
    (:audience message)))

(defmethod process-message! :others
  [{:keys [message connections send-fn]}]
  (doseq [connection connections]
    (send-fn {:message message
              :channel (:channel connection)})))

;;(defmethod process-message! :guests
;;  [{:keys [session-id] :as msg} ch connections]
;;  (let [targets (disj (->> (session-connections session-id connections)
;;                           (filter #(false? (:host %)))
;;                           (map :ch)
;;                           set)
;;                      ch)]
;;    (log/info (format "[%s] Forwarding messge to %d guests."
;;                      session-id
;;                      (count targets)))
;;    (doseq [c targets]
;;      (go (>! c msg)))))
;;
;;(defmethod process-message! :host
;;  [{:keys [session-id] :as msg} ch connections]
;;  (let [targets (disj (->> (session-connections session-id connections)
;;                           (filter #(true? (:host %)))
;;                           (map :ch)
;;                           set)
;;                      ch)]
;;    (log/info (format "[%s] Forwarding messge to %d host"
;;                      session-id
;;                      (count targets)))
;;    (doseq [c targets]
;;      (go (>! c msg)))))
;;
;;(defmethod process-message! :server
;;  [{:keys [session-id]} ch connections]
;;  (go
;;    (>! ch {:session-id  (str session-id)
;;            ;:host        host
;;            :instance-id :server
;;            :audience    :server
;;            :data        {}
;;            :ts          (int (-> (System/currentTimeMillis) (/ 1000)))})))

(defmethod process-message! :default
  [args]
  (log/warn "Unknown audience"))


