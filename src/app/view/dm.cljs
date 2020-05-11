(ns app.view.dm
  (:require
    [app.cursors :as cursors]
    [reagent.core :as r]
    [app.browser :as browser]
    [app.local-storage :as local-storage]
    [app.websocket-io :as ws]
    [app.state :as state]
    [app.view.components :refer [<app-title>
                                 <side-draw>
                                 <websocket-status>
                                 <token-card>
                                 <token-card-mini>
                                 <input>
                                 <container>
                                 <map-svg>]]))

(defn <dm-view>
  []
  (let [selected-token (r/atom nil)]
    @state/report-state-diffs
    @state/persist-state-changes
    (swap! state/local assoc :dm? true)
    (when-let [local-init (local-storage/get
                            {:dm? (:dm? @state/local)
                             :session-id (browser/session-id)})]
      (reset! state/shared local-init))
    (fn []
      (let [columns     (r/cursor state/shared [:map :width])
            rows        (r/cursor state/shared [:map :height])
            map-url     (r/cursor state/shared [:map :img-url])
            session-id  (r/track browser/session-id)
            ws-state    @ws/ready-state]
        [:<>
         [<side-draw>
          {}
          [<container>
           {:title "map settings"}

           [<input> {:label "player link"
                     :value (str "./join?session=" session-id)}]

           [<input> {:label "columns"
                     :type "number"
                     :min 1
                     :value @columns
                     :on-change #(reset! columns (int %))}]

           [<input> {:label "rows"
                     :type "number"
                     :min 1
                     :value @rows
                     :on-change #(reset! rows (int %))}]

           [<input> {:label "map url"
                     :value @map-url
                     :on-change #(reset! map-url %)}]]

          [<container>
           {:title "tokens"}
           (doall
             (for [t @(cursors/tokens)]
               [<token-card> {:id (:id t)}]))]]
         [<app-title>]
         [:<>
          [<websocket-status>
           {:ready-state ws-state
            :style {:position :fixed
                    :right "0.5rem"
                    :bottom "0.5rem"}
            :on-click ws/ping!}]]
         [<map-svg>
          {:overlay-opacity 0.5
           :on-token-click #(reset! selected-token %)}]
         [<token-card-mini> {:id (:id @selected-token)}]]))))
