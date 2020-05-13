(ns app.view.player
  (:require
    [app.cursors :as cursors]
    [reagent.core :as r]
    [app.browser :as browser]
    ;[app.local-storage :as local-storage]
    [app.websocket-io :as ws]
    [app.state :as state]
    [app.view.components :refer [<app-title>
                                 <side-draw>
                                 <websocket-status>
                                 <btn>
                                 <token-card>
                                 <token-card-mini>
                                 <input>
                                 <container>
                                 <map-svg>]]))

(defn <player-view>
  []
  (let []
    ;@state/report-state-diffs
    ;@state/persist-state-changes
    (swap! state/local assoc :dm? false)
    (fn []
      (let [columns     (cursors/map-cols)
            rows        (cursors/map-rows)
            map-url     (cursors/map-img-url)
            session-id  (r/track browser/session-id)
            ws-state    @ws/ready-state]
        [:<>
         [<app-title>]
         [:<>
          [<websocket-status>
           {:ready-state ws-state
            :style {:position :fixed
                    :right "0.5rem"
                    :bottom "0.5rem"}
            :on-click ws/ping!}]]
         [<map-svg>
          {:overlay-opacity 1}]]))))
