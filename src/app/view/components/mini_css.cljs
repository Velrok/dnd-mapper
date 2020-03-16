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
   children])

(defn <card>
  [props & children]
  [:div.card
   children])
