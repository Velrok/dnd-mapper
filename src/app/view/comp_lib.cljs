(ns app.view.comp-lib
  (:require
    [app.browser :refer [log!]]
    [reagent.core :as r]
    [app.view.components :refer [<btn>
                                 <websocket-status>
                                 <btn-group>
                                 <token>
                                 <container>
                                 <input>
                                 <progress>
                                 <token-card>
                                 <token-card-mini>
                                 <map-svg>
                                 <token-svg>
                                 <switch>]]))

(defn <comp-lib-view>
  []
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
         :key c
         :max 10
         :color c}]))

   [:h2 "Input"]
   [<input> {:value "23"
             :type "number"
             :on-change prn}]

   [:h2 "Websocket status"]
   (doall
     (for [[label value] [["CONNECTING" 0]
                          ["OPEN" 1] 
                          ["CLOSING" 2]
                          ["CLOSED" 3]]]
       [:<>
        {:key label}
        [:p label]
        [<websocket-status>
         {:ready-state value}]]))



   [:h2 "Token card"]
   [<token-card> {:id "goblin"}]
   [<token-card-mini> {:id "goblin"}]

   [:h2 "Token"]
   [<token> {:id "goblin"}]

   [:h2 "Map"]
   [<map-svg>]])
