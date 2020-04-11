(ns app.view.comp-lib
  (:require
    [app.browser :refer [log!]]
    [reagent.core :as r]
    [app.view.components :refer [<btn>
                                 <btn-group>
                                 <token>
                                 <container>
                                 <input>
                                 <progress>
                                 <player-card>
                                 <map-svg>
                                 <token-svg>
                                 <switch>]]))

(defn <comp-lib-view>
  []
  (let [player (r/atom {:id "player"
                        :name "Udrik"
                        :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"
                        :hp 60
                        :max-hp 100
                        :player-visible true
                        :initiative 23
                        :dm-focus true})]
    (fn []
      [:<>
       [:h1 "Components library"]

       [:h2 "Buttons"]
       (for [c ["primary" "success" "warning" "error"]]
         [<btn>
          {:key (gensym)
           :color c
           :on-click #(log! "button pressed" c)}
          c])

       [:h2 "Button group"]
       [<btn-group> {}
        [<btn> {} "reveil"]
        [<btn> {} "hide"]]

       [:h2 "Switch"]
       [<switch> {:options [{:id "reveil" :label "reveil"}
                            {:id "hide" :label "hide"}]
                  :selected "reveil"
                  :on-click #(prn %)}]

       [:h2 "Container"]
       [<container>
        {:title "Container"}
        [:p "With text"]]

       [:h2 "Progress"]
       (doall
         (for [c ["default" "primary" "warning" "error" "pattern"]]
           [<progress>
            {:value 5
             :max 10
             :color c}]))

       [:h2 "Input"]
       [<input> {:value "23"
                 :type "number"
                 :on-change prn}]


       [:h2 "Player card"]
       [<player-card> {:player player
                       :on-change #(do (prn %)
                                       (reset! player %))}]

       [:h2 "Token"]
       [<token> {:dm? (delay true)} player]

       [:h2 "Map"]
       [<map-svg>
        {:img-url "https://i.imgur.com/xjnVCUk.jpg"
         :w 30
         :h 30
         :on-cell-reveil #(prn "reveil" %)
         :on-cell-hide #(prn "hide" %)
         :overlay-opacity 0.5
         :tokens [[<token-svg> {:img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"}]
                  [<token-svg> {:x 3 :size :large :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"}]]
         ;:on-cell-click #(prn "cell click " %)
         }]

       ])))
