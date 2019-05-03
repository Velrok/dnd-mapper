(ns app.message-processing
  (:require
    [app.websocket-io :as ws]
    [app.state :as state]
    [re-frame.core :as rf]
    [app.browser :as browser]))

(defn process-server-message!
  [{:keys [message]}]
  (when (browser/debug?)
    (println (str "[" (-> message :data :type) "] < "
                  (prn-str message))))
  (rf/dispatch [(keyword
                 (str "server-msg-" (or (some-> message :data :type)
                                        "no-type")))
                message]))

(rf/reg-event-db
  :server-msg-no-type
  (fn [db msg]
    (assoc-in db [:last-server-response-time]
              (-> msg :data :at (js/Date.)))))
