(ns app.routing
  (:require
    [app.state :as state]
    ;;[app.view.layout :refer [<app>]]
    [app.view.dm :refer [<dm-view>]]
    [app.view.comp-lib :refer [<comp-lib-view>]]
    [app.views :refer [<player-view> <home>]]
    [app.browser :refer [log!] :as browser]))

(def routes
  {"/"         <home>
   "/comp-lib" <comp-lib-view>
   "/join"     <player-view>
   "/dm"       <dm-view>})

(defn handle-location-change
  [[event-type {:keys [url]}]]
  (if (= event-type :app.browser/goto)
    (when-let [c (get routes url nil)]
      (reset! state/current-view c))
    ::skipp))

(def router
  (delay
    (.addEventListener js/window
                       "popstate"
                       #(let [uri (browser/current-uri)]
                          (tap> [::browser/goto {:target (str uri)
                                                 :url    (:path uri)
                                                 :params (:query uri)}])))
    (add-tap handle-location-change)))

