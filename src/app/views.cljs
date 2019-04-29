(ns app.views
  (:require
    [app.browser :as browser]
    [reagent.core :as r]
    [app.state :as state]))

(def ^:const dead-icon "data:image/png;base64,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")

(defn <token>
  ([player]
   (<token> {} player))
  ([attr player]
   (let [{:keys [id img-url]} player]
     [:div.token
      (merge
        {:id id
         :key (str "player-id-" id)
         :style {:background-image (str "url(" (if (:dead player)
                                                 dead-icon
                                                 img-url) ")")}
         :draggable true
         :on-drag-start (fn [e]
                          (-> e
                              .-dataTransfer
                              (.setData "player-id" id)))}
        attr)])))

(defn <start>
  []
  (let [u-name          (r/atom (gensym "User"))
        join-session-id (get-in (browser/current-uri) [:query "join-session-id"])]
    (if-not join-session-id
      [:div
       [:h2 (str "create session")]
       [:input.size-medium
        {:type "text"
         :value @u-name
         :on-change #(reset! u-name (some-> % .-target .-value))}]
       [:button.btn
        {:on-click #(do (reset! state/username @u-name)
                        (reset! state/active-view-id :session-new))}
        ">>"]]

      [:div
       [:h2 "join session " join-session-id]
       [:input {:type "text"
                :value @u-name
                :on-change #(reset! u-name (some-> % .-target .-value))}]
       [:button 
        {:on-click #(do (reset! state/username @u-name)
                        (reset! state/active-view-id :session-join))}
        ">>"]])))


(defn <map-definition-input>
  [attr]
  (fn []
    [:div#map-definition-input
     attr
     [:fieldset
      [:label {:for "#map-url"} "url"]
      [:input#map-url.pull-right
       {:type :url
        :value (:img-url @state/dnd-map)
        :on-change #(swap! state/dnd-map assoc :img-url (some-> % .-target .-value))
        }]]

     [:fieldset
      [:label {:for "#map-width"} "columns"]
      [:input#map-width.pull-right
       {:type :number
        :value @state/map-width
        :min 1
        :on-change #(reset! state/map-width (some-> % .-target .-value int))
        }]]

     [:fieldset
      [:label {:for "#map-height"} "rows"]
      [:input#map-height.pull-right
       {:type :number
        :min 1
        :value @state/map-height
        :on-change #(reset! state/map-height (some-> % .-target .-value int))
        }]]

     [:fieldset
      [:label {:for "#map-height"} "highlight overlay"]
      [:input#highlight-overlay.pull-right
       {:type :checkbox
        :checked @state/highlight-overlay
        :on-change #(reset! state/highlight-overlay (some-> % .-target .-checked))
        }]]

     [:fieldset
      [:label {:for "#is-dm"} "DM"]
      [:strong.pull-right (if @state/dm? "yes" "no")]]

     (when @state/dm?
       [:fieldset
        [:label {:for "#fog-of-war-mode"
                 :style {:display "block"}} "Fog of war: "]

        [:div.pull-right
         [:label {:for "#fog-of-war-mode-reveil"
                  :style {:margin-right "0.5em"}} "reveil"]
         [:input#fog-of-war-mode-reveil
          {:type :radio
           :name "fog-of-war-mode"
           :checked (= :reveil @state/fog-of-war-mode)
           :on-change #(reset! state/fog-of-war-mode :reveil)}]

         [:label {:for "#fog-of-war-mode-obscure"
                  :style {:margin-right "0.5em"
                          :margin-left "1em"}} "obscure"]
         [:input#fog-of-war-mode-obscure
          {:type :radio
           :name "obscure"
           :checked (= :obscure @state/fog-of-war-mode)
           :on-change #(reset! state/fog-of-war-mode :obscure) }]]])]))

(defn <token-list>
  [attr]
  [:ul#characters-list
   (merge {:style {:height "100px"}}
          attr)
   (doall
     (for [p (vals @state/players)]
       [:li.flex-cols.character-list-entry
        {:key (str "char-list-" (:id p))
         :class [(when-not (:player-visible p)
                   (if @state/dm?
                     "player-invisible-dm-mode"
                     "player-invisible"))]}
        [<token> p]
        [:div.flex-rows
          [:p (:name p)]
          [:input {:type "text"
                  :default-value (:img-url p)}]
          (when @state/dm?
            [:div.flex-cols
              [:label "Player visible"]
              [:input {:type :checkbox
                       :on-change #(swap! state/players
                                          assoc-in
                                          [(:id p) :player-visible]
                                          (some-> % .-target .-checked))
                       :checked (:player-visible p)}]])
          (when @state/dm?
            [:div.flex-cols
              [:label "Dead?"]
              [:input {:type :checkbox
                       :on-change #(swap! state/players
                                          assoc-in
                                          [(:id p) :dead]
                                          (some-> % .-target .-checked))
                     :checked (:dead p)}]])]]))
   (when @state/dm?
     [:li {:key "char-list-placeholder"}
      (let [n (r/atom nil)
            img (r/atom nil)
            p-id (str (gensym "enemy"))]
        [:div.flex-cols
         [:div.token
          {:style {:background-image (str "url(https://svgsilh.com/svg_v2/1270001.svg)")}
           :on-click #(swap! state/players
                             assoc
                             p-id
                             {:id             p-id
                              :name           @n
                              :img-url        @img
                              :player-visible false
                              :on-map         false
                              :dead           false
                              :position       nil})}]
         [:div.flex-rows
          [:p "Add"]
          [:input {:type "text"
                   :placeholder (str "Enemy" (count @state/players))
                   :on-change #(reset! n (-> % .-target .-value))}]
          [:input {:type "text"
                   :placeholder (str "http://")
                   :on-change #(reset! img (-> % .-target .-value))}]]])])])

(defn <map>
  [attr]
  (fn []
    [:div#map
     attr
     [:div.map-wrapper
      {:class [(when @state/dm?
                 "dm-mode")]
       :style (when @state/dm?
                {:cursor (case @state/fog-of-war-mode
                           :reveil "copy"
                           :obscure "no-drop")})}
      [:img.map-img {:src (:img-url @state/dnd-map)
                             :alt (:alt @state/dnd-map)}]
      [:table.map-table
       [:tbody.map-tbody
        {:style {:top "0px" :left "0px" :right "0px" :bottom "0px"}}
        (doall
          (for [y (range @state/map-height)]
            [:tr.map-row {:key (str "m-prev-y" y)}
             (doall
               (for [x (range @state/map-width)]
                 (let [pos {:x x :y y}
                       surrounding (for [dx (range -1 2)
                                         dy (range -1 2)]
                                     (-> pos
                                         (update :x (partial + dx))
                                         (update :y (partial + dy))))]
                 [:td.map-cell
                  {:key (str "map-prev-yx-" y x)
                   :on-mouse-over (when @state/dm?
                                    (fn [e]
                                      (when (= 1 (.-buttons e))
                                        (case @state/fog-of-war-mode
                                          :reveil  (swap! state/reveiled-cells #(apply conj % surrounding))
                                          :obscure (swap! state/reveiled-cells #(apply disj % surrounding))))))
                  ; :on-click #_(fn [e]
                  ;             (if (contains? @state/reveiled-cells pos)
                  ;              (swap! state/reveiled-cells dissoc pos)
                  ;              (swap! state/reveiled-cells assoc pos nil)))
                   :on-drag-over #(.preventDefault %)
                   :on-drop (fn [e]
                              (.preventDefault e)
                              (when-let [id (some-> e .-dataTransfer (.getData "player-id"))]
                                (when-let [p (some->> @state/players
                                                      vals
                                                      (filter (fn [p] (= id (:id p))))
                                                      first)]
                                  (swap! state/players assoc-in [(:id p) :position] pos))))
                   :class [(when @state/highlight-overlay
                             "map-cell__highlight")
                           (when-not (contains? @state/reveiled-cells
                                                pos)
                             "fog-of-war")]}
                  (when-let [p (some->> @state/players
                                        vals
                                        (filter
                                          (fn [p] (= pos (:position p))))
                                        first)]
                    [<token> {:class (when-not (:player-visible p)
                                             (if @state/dm?
                                               "player-invisible-dm-mode"
                                               "player-invisible"))}
                     p])])))]))]]]]))


(defn <session-new>
  [{:keys [state-init
           session-id]}]
  @state-init
  (fn []
    [:div#session-new.flex-rows
     [:p "player link: "
      (let [link (str (assoc-in (browser/current-uri)
                                [:query "join-session-id"]
                                @session-id))]
        [:input#player-join-url
         {:type :text
          :read-only true
          :value link
          :on-click
          (fn []
            (.select (js/document.getElementById "player-join-url"))
            (js/document.execCommand "copy")
            (js/alert "player link copied"))}])]
     [:div.game.flex-cols
      [<map> {:style {:width "100%"}}]
      [:div.flex-rows
       {:style {:min-width "13em"
                :padding-left "7px"}}
       [<map-definition-input>]
       [<token-list>]]]
     [:pre (str "session id: " @session-id)]]))


(defn <session-join>
  [{:keys [state-init
           session-id]}]
  @state-init
  [:div
   [:h2 "Join session " @session-id]
   [:div.flex-cols
    [<map> {:style {:width "100%"}}]
    [:div.flex-rows
     {:style {:min-width "13em"
              :padding-left "7px"}}
     ;[<map-definition-input>]
     [<token-list>]]]])
