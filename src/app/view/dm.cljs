(ns app.view.dm
  (:require
    [reagent.core :as r]
    [app.browser :as browser]
    [app.websocket-io :as ws]
    [app.state :refer [state] :as app-state]
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
    @app-state/report-state-diffs
    (fn []
      (let [dungeon-map (:map @state)
            columns (r/cursor state [:map :width])
            rows (r/cursor state [:map :height])
            tokens (vals (:players @state))
            session-id (r/track #(some-> (browser/current-uri) :query (get "session")))
            ws-state @ws/ready-state]
        [:<>
         [<side-draw>
          {}
          [<container>
           {:title "map settings"}
           [<input> {:label "player link" :value (str "./join?s=" session-id)}]
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
                     :type :number
                     :value (:img-url dungeon-map)}]]
          [<container>
           {:title "tokens"}
           (doall
             (for [t tokens]
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
