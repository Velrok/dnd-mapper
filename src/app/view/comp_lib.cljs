(ns app.view.comp-lib
  (:require
    [app.browser :refer [log!]]
    [app.view.components :refer [<token> <map-svg> <token-svg>]]
    [app.view.components.mini-css
     :refer [<button>
             <button-group>
             <link>
             <accordion>
             <card>
             <section>
             <switch>
             <player-card>
             <accordion-section>]]))

(defn <comp-lib-view>
  []
  (let [player {:id "player"
                :name "Udrik"
                :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"
                :hp 60
                :max-hp 100
                :player-visible true
                :initiative 23
                :dm-focus true}]
    [:<>
     [:h1 "Components library"]

     [:h2 "Link"]
     [:span "This " [<link> {} "link"] " will take you somewhere." ]

     [:h2 "Buttons"]
     (for [c ["primary" "secondary" "tertiary" "inverse"]]
       [<button>
        {:key (gensym)
         :color c
         :on-click #(log! "button pressed" c)}
        c])

     [:h2 "Button group"]
     [<button-group> {}
      [<button> {} "reveil"]
      [<button> {} "hide"]]

     [:h2 "Switch"]
     [<switch> {:options [{:id "reveil" :label "reveil"}
                          {:id "hide" :label "hide"}]
                :selected "reveil"
                :on-click #(prn %)}]

     [:h2 "Card"]
     [<card> {}
      [<section> {}
       [:h3 "card"]
       [:p "this is a card"]]]

     [:h2 "Player card"]
     [<player-card> {:player player}]

     [:h2 "Accordion"]
     [<accordion> {}
      [<accordion-section>
       {:key (gensym)
        :checked true
        :label "Nav"}
       [:p {:key (gensym)} [<link> {:href "/home"} "Home"]]
       [:p {:key (gensym)} [<link> {:href "/comp-lib"} "Comp lib"]]]]


     [:h2 "Token"]
     [<token> {:dm? (delay true)} player]

     [:h2 "Map"]
     [<map-svg>
      {:img-url "https://i.imgur.com/xjnVCUk.jpg"
       :w 30
       :h 30
       :overlay-opacity 0.5
       :tokens [[<token-svg> {:img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"}]
                [<token-svg> {:x 3 :size :large :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"}]]
       ;:on-cell-click #(prn "cell click " %)
       }]

     ]))
