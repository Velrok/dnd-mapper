(ns app.view.components.mini-css
  (:require
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
              :checked (:checked props false)
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

  ;<input type="radio" id="accordion-section1" checked aria-hidden="true" name="accordion">
  ;<label for="accordion-section1" aria-hidden="true">Accordion section 1</label>
  ;<div>
  ;  <p>This is the first section of the accordion</p>
  ;</div>
