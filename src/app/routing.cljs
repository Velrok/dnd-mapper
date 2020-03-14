(ns app.routing
  (:require [app.browser :refer [log!]]))

(defmulti handle-location-change
  (fn [[event-type {:keys [url params]}]]
    (if (= event-type :app.browser/goto)
      [url params]
      ::skipp)))

(defmethod handle-location-change :default
  [[_ {:keys [url]}]]
  (log! (str "No handler for " url)))

(defmethod handle-location-change ::skipp
  [& more]
  (log! ::skipp more))

(def router
  (delay
    (add-tap handle-location-change)))

