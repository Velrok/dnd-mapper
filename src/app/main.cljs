(ns app.main
  (:require [reagent.core :as r]))

(defonce active-view-id (r/atom ::home))

(defonce highlight-overlay (r/atom true))
(defonce map-width  (r/atom 35))
(defonce map-height (r/atom 50))
(defonce dm?        (r/atom false))

(defonce fog-of-war-mode (r/atom ::reveil)) ; ::obscure

(defonce reveiled-cells (r/atom {}))

(defonce dnd-map
  (r/atom {:img-url "https://img00.deviantart.net/d36a/i/2015/115/3/0/abandoned_temple_of_blackfire_by_dlimedia-d4pponv.jpg"
           :alt "Created by DLIMedia: https://www.deviantart.com/dlimedia/art/Abandoned-Temple-of-Blackfire-285053467"}))

(defonce players
  (r/atom
    [(r/atom {:id (str (gensym "player"))
              :name "Negwen"
              :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/4729/162/150/300/636756769380492799.png"
              :player-visible true
              :on-map false
              :position nil})
     (r/atom {:id (str (gensym "player"))
              :name "Ikara"
              :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/17/747/150/150/636378331895705713.jpeg"
              :player-visible true
              :on-map false
              :position nil})
     (r/atom {:id (str (gensym "player"))
              :name "Udrik"
              :img-url "https://media-waterdeep.cursecdn.com/avatars/thumbnails/10/71/150/150/636339380148524382.png"
              :player-visible true
              :on-map false
              :position nil})]))

(defn <char-avatar>
  ([player]
   (<char-avatar> {} player))
  ([attr player]
   (let [{:keys [id img-url]} @player]
     [:div.char-avatar
      (merge
        {:id id
         :key (str "player-id-" id)
         :style {:background-image (str "url(" img-url ")")}
         :draggable true
         :on-drag-start (fn [e]
                          (-> e
                              .-dataTransfer
                              (.setData "player-id" id)))}
        attr)])))

(defn <nav>
  []
  [:nav
   [:ul
    [:li {:on-click #(reset! active-view-id ::home)} "home"]
    [:li {:on-click #(reset! active-view-id ::session-new)} "new session"]
    [:li {:on-click #(reset! active-view-id ::session-join)} "join session"]]])

(defn <home>
  []
  [:div
   [:h1 "home view"]
   [:button {:on-click #(reset! active-view-id ::session-new)}
    "new session"]
   [:button "join session"]])


(defn <map-definition-input>
  [attr]
  (fn []
    [:div#map-definition-input
     attr
     [:fieldset
      [:label {:for "#map-url"} "url"]
      [:input#map-url
       {:type :url
        :value (:img-url @dnd-map)
        :on-change #(swap! dnd-map assoc :img-url (some-> % .-target .-value))
        }]]

     [:fieldset
      [:label {:for "#map-width"} "width"]
      [:input#map-width
       {:type :number
        :value @map-width
        :min 1
        :on-change #(reset! map-width (some-> % .-target .-value int))
        }]]

     [:fieldset
      [:label {:for "#map-height"} "height"]
      [:input#map-height
       {:type :number
        :min 1
        :value @map-height
        :on-change #(reset! map-height (some-> % .-target .-value int))
        }]]

     [:fieldset
      [:label {:for "#map-height"} "highlight overlay"]
      [:input#highlight-overlay
       {:type :checkbox
        :checked @highlight-overlay
        :on-change #(reset! highlight-overlay (some-> % .-target .-checked))
        }]]

     [:fieldset
      [:label {:for "#is-dm"} "DM mode?"]
      [:input#is-dm
       {:type :checkbox
        :checked @dm?
        :on-change #(reset! dm? (some-> % .-target .-checked))
        }]]

     (when @dm?
       [:fieldset
        [:label {:for "#fog-of-war-mode"} "Fog of war: "]

        [:label {:for "#fog-of-war-mode-reveil"
                 :style {:margin-right "0.5em"}} "reveil"]
        [:input#fog-of-war-mode-reveil
         {:type :radio
          :name "fog-of-war-mode"
          :checked (= ::reveil @fog-of-war-mode)
          :on-change #(reset! fog-of-war-mode ::reveil)}]

        [:label {:for "#fog-of-war-mode-obscure"
                 :style {:margin-right "0.5em"
                         :margin-left "1em"}} "obscure"]
        [:input#fog-of-war-mode-obscure
         {:type :radio
          :name "obscure"
          :checked (= ::obscure @fog-of-war-mode)
          :on-change #(reset! fog-of-war-mode ::obscure) }]])
     ]))

(defn <characters-list>
  [attr]
  [:ul (merge {:style {:height "100px"}}
              attr)
   (doall
     (for [p @players]
       [:li.flex-cols {:key (str "char-list-" (:id @p))}
        [<char-avatar> p]
        [:div.flex-rows
         [:p (:name @p)]
         [:input {:type "text"
                  :value (:img-url @p)}]
         [:div.flex-cols
          [:label "Player visible"]
          [:input {:type :checkbox
                   :on-change #(swap! p assoc :player-visible (some-> % .-target .-checked))
                   :checked (:player-visible @p)}]]]]))
   [:li {:key "char-list-placeholder"}
    (let [n (r/atom nil)
          img (r/atom nil)]
      [:div.flex-cols
       [:div.char-avatar
        {:style {:background-image (str "url(https://svgsilh.com/svg_v2/1270001.svg)")}
         :on-click #(do
                      (prn "add enemy")
                      (swap! players
                             conj
                             (r/atom
                               {:id             (str (gensym "enemy"))
                                :name           @n
                                :img-url        @img
                                :player-visible false
                                :on-map         false
                                :position       nil})))}]
       [:div.flex-rows
        [:p "Add"]
        [:input {:type "text"
                 :placeholder (str "Enemy" (count @players))
                 :on-change #(reset! n (-> % .-target .-value))}]
        [:input {:type "text"
                 :placeholder (str "http://")
                 :on-change #(reset! img (-> % .-target .-value))}]]])]])

(defn <map-preview>
  [attr]
  (fn []
    [:div#map-preview
     attr
     [:div.map-preview-wrapper
      {:class [(when @dm?
                 "dm-mode")]
       :style {:cursor (case @fog-of-war-mode
                        ::reveil "copy"
                        ::obscure "no-drop")}}
      [:img.map-preview-img {:src (:img-url @dnd-map)
                             :alt (:alt @dnd-map)}]
      [:table.map-preview-table
       [:tbody.map-preview-tbody
        {:style {:top "0px" :left "0px" :right "0px" :bottom "0px"}}
        (doall
          (for [y (range @map-height)]
            [:tr.map-preview-row {:key (str "m-prev-y" y)}
             (doall
               (for [x (range @map-width)]
                 (let [pos {:x x :y y}]
                 [:td.map-preview-cell
                  {:key (str "map-prev-yx-" y x)
                   :on-mouse-over (fn [e]
                                    (when (= 1 (.-buttons e))
                                      (case @fog-of-war-mode
                                        ::reveil  (swap! reveiled-cells assoc pos)
                                        ::obscure (swap! reveiled-cells dissoc pos))))
                   :on-click (fn [e]
                               (if (contains? @reveiled-cells pos)
                                (swap! reveiled-cells dissoc pos)
                                (swap! reveiled-cells assoc pos nil)))
                   :on-drag-over #(.preventDefault %)
                   :on-drop (fn [e]
                              (.preventDefault e)
                              (when-let [id (some-> e .-dataTransfer (.getData "player-id"))]
                                (when-let [p (some->> @players
                                                      (filter (fn [p] (= id (:id @p))))
                                                      first)]
                                  (swap! p assoc-in [:position] pos))))
                   :class [(when @highlight-overlay
                             "map-cell__highlight")
                           (when-not (contains? @reveiled-cells
                                                pos)
                             "fog-of-war")]}
                  (when-let [p (some->> @players
                                        (filter
                                          (fn [p] (= pos (:position @p))))
                                        first)]
                    [<char-avatar> {:class (when-not (:player-visible @p)
                                             (if @dm?
                                               "player-invisible-dm-mode"
                                               "player-invisible"))}
                     p])])))]))]]]]))


(defn <session-new>
  []
  [:div#session-new.flex-rows
   [:h2 "Sesson New "]
   [:div.flex-cols
    [<map-preview> {:style {:width "100%"}}]
    [:div.flex-rows
     [<map-definition-input>]
     [<characters-list>]]]])

(defn <session-join>
  []
  [:h2 "Sesson JOIN"])

(def views
  {::home         <home>
   ::session-new  <session-new>
   ::session-join <session-join>
   })

(defn app
  []
  [:div
   [:h1 "DND Mapper"]
   [<nav>]
   (let [active-view (get views @active-view-id)]
     [active-view])])

(defn ^:dev/after-load render
  []
  (r/render [app] (js/document.getElementById "app")))

(defn ^:export  main
  []
  (render))
