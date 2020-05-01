(ns app.view.dm
  (:require
    [reagent.core :as r]
    [app.browser :as browser]
    [app.local-storage :as local-storage]
    [app.websocket-io :as ws]
    [app.state :as state]
    [app.view.components :refer [<app-title>
                                 <side-draw>
                                 <websocket-status>
                                 <token-card>
                                 <input>
                                 <container>
                                 <map-svg>]]))

(defn <dm-view>
  []
  (do
    @state/report-state-diffs
    @state/persist-state-changes
    (reset! state/shared (local-storage/get {:dm? true :session-id (browser/session-id)}))
    (swap! state/local assoc :dm? true)
    (fn []
      (let [columns     (r/cursor state/shared [:map :width])
            rows        (r/cursor state/shared [:map :height])
            map-url     (r/cursor state/shared [:map :img-url])
            tokens      (r/track #(some-> @state/shared :players vals))
            selected-token (r/cursor state/local [:selected-token])
            session-id  (r/track browser/session-id)
            ws-state    @ws/ready-state]
        [:<>
         [<side-draw>
          {}
          ^{:key (gensym "side-draw")}
          [<container>
           {:title "map settings"}

           ^{:key (gensym "map-settings-item-")}
           [<input> {:label "player link"
                     :value (str "./join?session=" session-id)}]

           ^{:key (gensym "map-settings-item-")}
           [<input> {:label "columns"
                     :type "number"
                     :min 1
                     :value @columns
                     :on-change #(reset! columns (int %))}]

           ^{:key (gensym "map-settings-item-")}
           [<input> {:label "rows"
                     :type "number"
                     :min 1
                     :value @rows
                     :on-change #(reset! rows (int %))}]

           ^{:key (gensym "map-settings-item-")}
           [<input> {:label "map url"
                     :value @map-url
                     :on-change #(reset! map-url %)}]]

          ^{:key (gensym "side-draw")}
          [<container>
           {:title "tokens"}
           (doall
             (for [t @tokens]
               ^{:key (str t)}
               [<token-card> {:token (delay t)
                              :on-change prn}]))]]
         [<app-title>]
         [:<>
          [<websocket-status>
           {:ready-state ws-state
            :style {:position :fixed
                    :right "0.5rem"
                    :bottom "0.5rem"}
            :on-click ws/ping!}]]
         [<map-svg>
          {:img-url map-url
           :w columns
           :h rows
           :on-cells-reveil #(prn "reveil" %)
           :on-cells-hide #(prn "hide" %)
           :overlay-opacity 0.5
           :on-token-click #(reset! selected-token %)
           :tokens tokens}]
         [<token-card> {:token selected-token}]]))))
