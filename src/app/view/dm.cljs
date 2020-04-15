(ns app.view.dm
  (:require
    ;[reagent.core :as r]
    [app.state :refer [state]]
    [app.view.components :refer [<app-title>
                                 <side-draw>
                                 <token-card>
                                 <input>
                                 <container>
                                 <map-svg>]]))


(defn <dm-view>
  []
  (let [dungeon-map (:map @state)
        tokens (vals (:players @state))]
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
     [<map-svg>
      {:img-url (:img-url dungeon-map)
       :w (:width dungeon-map)
       :h (:height dungeon-map)
       :on-cells-reveil #(prn "reveil" %)
       :on-cells-hide #(prn "hide" %)
       :overlay-opacity 0.5
       :tokens tokens}]]))
