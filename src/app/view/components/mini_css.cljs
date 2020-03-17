(ns app.view.components.mini-css
  (:require
    [reagent.core :as r]
    [app.browser :as browser]))


(defn <header>
  [{:keys []} & children]
  [:header
   children])

(defn <button>
  [props & children]
  (let [passdown-props (dissoc props :color :size :class)]
    [:button (merge {:class (str (:color props) " "
                                 (:size props) " "
                                 (:class props))}
                    passdown-props)
     children]))

(defn <button-group>
  [props & children]
  [:div.button-group
   children])

(defn <switch>
  [props & children]
  (let [{:keys [options selected on-click]} props
        selection (r/atom selected)]
    (prn "on-c" on-click)
    (fn []
      [:div.button-group
       (doall
         (for [{:keys [id label]} options]
           [<button>
            {:key (gensym)
             :on-click #(do
                          (reset! selection id)
                          (when on-click (on-click id)))
             :color (if (= id @selection)
                      "inverse"
                      "default")}
            label]))])))



(defn <link>
  [props & children]
  [:button (merge {:on-click #(browser/goto! (:href props "#")
                                             (:params props {}))}
                  (dissoc props :href :params)
                  {:class "mapper-link"})
   children])

(defn <accordion-section>
  [props & children]
  (let [id (:id props (gensym "accordion-section"))]
    [:<>
     [:input {:type "radio"
              :id id
              :default-checked (:checked props false)
              :aria-hidden "true"
              :name "accordion"}]
     [:label {:for id
              :aria-hidden "true"}
      (:label props "")]
     [:div
      children]]))

(defn <accordion>
  [props & children]
  [:div.collapse
   children])

(defn <section>
  [props & children]
  [:div.section
   props
   children])

(defn <card>
  [props & children]
  (let [{:keys [modifier color]} props]
    [:div.card
     {:class (str "shadowed " modifier " " color)}
     children]))

(defn <progress>
  [{:keys [value max color inline?]}]
  [:progress.inline {:class (str color " " (when inline? "inline"))
                     :value value
                     :max max}])

(defn <player-card>
  [{:keys [player]}]
  (let [{:keys [initiative name img-url hp max-hp player-visible]} player]
    [:div.player-card
     [<card> {:modifier "small"}

      [<section> {:class "player-card__header"}
       [:div.flex-cols
        [:img.rounded.bordered {:src img-url
                       :style {:max-height "3em"}}]
        [:h3 {} name]
        [:span.player-card__initiative-mark [:mark initiative]]]
       [:div.flex-cols
        [<progress> {:inline? true
                     :value hp
                     :max max-hp
                     :color (let [rel (/ hp max-hp)]
                              (cond
                                (< (/ 2 3) rel) "default"
                                (< (/ 1 3) rel) "primary"
                                :otherwise "secondary"))}]
        [:span hp " / " max-hp]]]

      [<section> {}
       [:div.flex-cols
        [:span "Player visible"]
        [<switch> {:options [{:id true :label "yes"}
                             {:id false :label "no"}]
                   :selected player-visible}]]]

      ]]))
