(ns app.view.dm
  (:require
    [reagent.core :as r]
    [app.browser :as browser]
    [app.websocket-io :as ws]
    [app.state :refer [state]]
    [app.view.components :refer [<app-title>
                                 <side-draw>
                                 <websocket-status>
                                 <token-card>
                                 <input>
                                 <container>
                                 <map-svg>]]))


(defn <dm-view>
  []
  (let [dungeon-map (:map @state)
        tokens (vals (:players @state))
        session-id (r/track #(some-> (browser/current-uri) :query (get "session")))
        ws-state @ws/ready-state]
    [:<>
     [<side-draw>
      {}
      [<container>
       {:title "map settings"}
       [<input> {:label "player link" :value "./join?s=asdf"}]
       [<input> {:label "columns" :value (:width dungeon-map)}]
       [<input> {:label "rows"    :value (:height dungeon-map)}]
       [<input> {:label "map url" :value (:img-url dungeon-map)}]]
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
        :on-click #(do
                     (.log js/console "send ws ping" @session-id)
                     (ws/send! "ping" {:session-id @session-id}))}]]
     [<map-svg>
      {:img-url (:img-url dungeon-map)
       :w (:width dungeon-map)
       :h (:height dungeon-map)
       :on-cells-reveil #(prn "reveil" %)
       :on-cells-hide #(prn "hide" %)
       :overlay-opacity 0.5
       :tokens tokens}]]))
