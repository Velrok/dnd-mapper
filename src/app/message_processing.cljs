(ns app.message-processing
  (:require
    [re-frame.core :as rf]
    [app.browser :as browser]))

(defn process-server-message!
  [{:keys [message]}]
  (when (browser/debug?)
    (println (str "[" (-> message :data :type) "] < "
                  (pr-str message))))
  (rf/dispatch [(keyword
                 (str "server-msg-" (or (some-> message :data :type)
                                        "no-type")))
                message]))

(rf/reg-event-db
  :server-msg-no-type
  (fn [db msg]
    (assoc-in db [:last-server-response-time]
              (-> msg :data :at (js/Date.)))))
