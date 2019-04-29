(ns app.browser
  (:require
    [cemerick.uri :refer [uri]]))

(defn current-uri
  []
  (-> js/window
      .-location
      .-href
      uri))

(def pp (.-log js/console))

(defn debug?
  []
  (if (some-> (current-uri)
              :query
              (get "debug"))
    true
    false))
