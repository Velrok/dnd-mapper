(ns app.routing
  (:require
    [app.state :as state]
    ;;[app.view.layout :refer [<app>]]
    [app.view.comp-lib :refer [<comp-lib-view>]]
    [app.views :refer [<player-view> <dm-view> <home>]]
    [app.browser :refer [log!]]))

(def routes
  {"/"         <home>
   "/comp-lib" <comp-lib-view>
   "/join"     <player-view>
   "/dm"       <dm-view>})

(defn handle-location-change
  [[event-type {:keys [url]}]]
  (if (= event-type :app.browser/goto)
    (when-let [c (get routes url nil)]
      (log! "setting view" c)
      (reset! state/current-view c))
    ::skipp))

(def router
  (delay
    (add-tap handle-location-change)))

