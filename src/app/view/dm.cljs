(ns app.view.dm
  (:require
    [reagent.core :as r]
    [app.browser :as browser]
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
    (fn []
      (let [dungeon-map (:map @state/shared)
            columns     (r/cursor state/shared [:map :width])
            rows        (r/cursor state/shared [:map :height])
            tokens      (vals (:players @state/shared))
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
                     :type :number
                     :value (:img-url dungeon-map)}]]

          ^{:key (gensym "side-draw")}
          [<container>
           {:title "tokens"}
           (doall
             (for [t tokens]
               ^{:key (str t)}
               [<token-card> {:token t
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
          {:img-url (:img-url dungeon-map)
           :w (:width dungeon-map)
           :h (:height dungeon-map)
           :on-cells-reveil #(prn "reveil" %)
           :on-cells-hide #(prn "hide" %)
           :overlay-opacity 0.5
           :tokens tokens}]]))))
