(ns app.message-processing
  (:require
    [app.websocket-io :as ws]
    [app.state :as state]
    [app.browser :as browser]))

(defmulti process-server-message!
  (fn [{:keys [message]}]
    (when (browser/debug?)
      (println (str "[" (-> message :data :type) "] < "
                    (prn-str message))))
    (-> message :data :type)))

(defmethod process-server-message! :default
  [{:keys [message]}]
  )

(defmethod process-server-message! ::reveiled-cells-reset
  [{:keys [message]}]
  (reset! state/reveiled-cells
          (some-> message :data :data)))

(defmethod process-server-message! ::players-reset
  [{:keys [message]}]
  (reset! state/players
          (some-> message :data :data)))

(defmethod process-server-message! ::dnd-map-reset
  [{:keys [message]}]
  (reset! state/dnd-map
          (some-> message :data :data)))

(defmethod process-server-message! ::map-width-reset
  [{:keys [message]}]
  (reset! state/map-width
          (some-> message :data :data)))

(defmethod process-server-message! ::map-height-reset
  [{:keys [message]}]
  (reset! state/map-height
          (some-> message :data :data)))

(defmethod process-server-message! ::request-state-init
  [_message]
  (prn [::process-server-message!-request-state-init])
  (when @state/dm?
    (doseq [m [{:type ::reveiled-cells-reset :data @state/reveiled-cells}
               {:type ::players-reset        :data @state/players}
               {:type ::dnd-map-reset        :data @state/dnd-map}
               {:type ::map-width-reset      :data @state/map-width}
               {:type ::map-height-reset     :data @state/map-height}]]
      (ws/send! m {:audience :guests}))))



