(ns app.view.comp-lib
  (:require
    [app.browser :refer [log!]]
    [app.view.components.mini-css
     :refer [<button>
             <link>
             <accordion>
             <accordion-section>]]))

(defn <comp-lib-view>
  []
  [:<>
   [:h1 "Components library"]

   [:h2 "Link"]
   [:span "This " [<link> {} "link"] " will take you somewhere." ]

   [:h2 "Buttons"]
   (for [c ["primary" "secondary" "tertiary" "inverse"]]
     [<button>
      {:color c
       :on-click #(log! "button pressed" c)}
      c])
   
   [:h2 "Accordion"]
   [<accordion> {}
    [<accordion-section>
     {:checked true
      :label "Nav"}
     [:p [<link> {:href "/home"} "Home"]]
     [:p [<link> {:href "/comp-lib"} "Comp lib"]]]]])
