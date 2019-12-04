(ns dnd-mapper.server.messages
  (:require
    [clojure.tools.logging :as log]
    [clojure.string :as string]
    [clojure.core.async :as a :refer [<! >! close! go]]))

(defn- session-connections
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


