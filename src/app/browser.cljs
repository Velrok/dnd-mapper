(ns app.browser
  (:require
    ["history" :refer [createBrowserHistory]]
    [cemerick.uri :refer [uri map->query]]))

(def history
  (delay (createBrowserHistory)))


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

(defn goto!
  [url params]
  (let [target (str url "?" (map->query params))]
    (tap> [::goto {:target target
                   :url url
                   :params params}])
    (.push @history
           target
           (clj->js params))))
