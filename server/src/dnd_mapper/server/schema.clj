(ns dnd-mapper.server.schema
  (:require
    [clojure.spec.alpha :as s]))

(s/def ::session-id int?)

(s/def ::name string?)
(s/def ::id int?)
(s/def ::is-dm boolean?)
(s/def ::url #"http[s]?://[\w/?=&.-_%]+")
(s/def ::avatar (s/keys :req-un [::url]))

(s/def ::player-visible boolean?)

(s/def ::character (s/keys :req-un [::name ::avatar ::player-visible]))
(s/def ::characters (s/every ::character))

(s/def ::player (s/keys :req-un [::id ::is-dm]
                        :opt-un [::name ::character]))
(s/def ::players (s/every ::player))

(s/def ::width pos?)
(s/def ::height pos?)
(s/def ::highlight-overlay boolean?)

(s/def ::map
  (s/keys :req-un [::width ::height ::url ::characters]))

(s/def ::x pos?)
(s/def ::y pos?)
(s/def ::position (s/keys :req-un [::x ::y]))
(s/def ::reveiled-positions (s/every ::position))

(s/def ::state
  (s/keys :req-un
          [::session-id ::players ::map ::reveiled-positions]))

(s/def :client-message/type #{:client-message/get-current-state :client-message/reset-current-state})

(defmulti client-message :client-message/type)
(defmethod client-message :client-message/get-current-state [_]
  (s/keys :req-un [::session-id :client-message/type]))

(defmethod client-message :client-message/reset-current-state [_]
  (s/keys :req-un [::session-id :client-message/type ::state]))


(s/def :server-message/type #{:server-message/reset-current-state})
(defmulti server-message :server-message/type)

(defmethod server-message :server-message/reset-current-state []
  (s/keys :req-un [::session-id ::state]))

