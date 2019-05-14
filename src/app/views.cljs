(ns app.views
  (:require
    [app.browser :as browser]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [app.websocket-io :as ws]
    [app.state :as state]
    [app.view.components :refer [<token-list> <token> <map>]]))

(defn <start>
  []
  (let [join-session-id (get-in (browser/current-uri) [:query "join-session-id"])]
    (if-not join-session-id
      [:div
       [:h2 (str "create")]
       [:button.btn
        {:on-click #(rf/dispatch [:host-session (-> (Math/random)
                                                    (* 100000000)
                                                    int
                                                    str)])}
        "create session >>"]]

      [:div
       [:h2 "join"]
       [:button.btn
        {:on-click #(rf/dispatch [:join-session join-session-id])}
        (str "join session "  join-session-id " >>")]])))


(defn <map-definition-input>
  [attr]
  (fn []
    [:div#map-definition-input
     attr
     [:fieldset
      [:label {:for "#map-url"} "url"]
      [:input#map-url.pull-right
       {:type :url
        :value @(rf/subscribe [:map-img-url])
        :on-change #(rf/dispatch [:map-img-url-changed
                                  (some-> % .-target .-value)])}]]

     [:fieldset
      [:label {:for "#map-width"} "columns"]
      [:input#map-width.pull-right
       {:type :number
        :value @(rf/subscribe [:map-width])
        :min 1
        :on-change #(rf/dispatch [:map-width-changed
                                  (some-> % .-target .-value int)])
        }]]

     [:fieldset
      [:label {:for "#map-height"} "rows"]
      [:input#map-height.pull-right
       {:type :number
        :min 1
        :value @(rf/subscribe [:map-height])
        :on-change #(rf/dispatch [:map-height-changed
                                  (some-> % .-target .-value int)])
        }]]

     [:fieldset
      [:label {:for "#map-height"} "highlight overlay"]
      [:input#highlight-overlay.pull-right
       {:type :checkbox
        :checked   @(rf/subscribe [:highlight-overlay])
        :on-change #(rf/dispatch [:highlight-overlay-changed
                                  (some-> % .-target .-checked)]) }]]

     [:fieldset
      [:label {:for "#is-dm"} "DM"]
      [:strong.pull-right (if @(rf/subscribe [:dm?]) "yes" "no")]]

     (when @(rf/subscribe [:dm?])
       [:fieldset
        [:label {:for "#fog-of-war-mode"
                 :style {:display "block"}} "Fog of war: "]

        [:div.pull-right
         [:label {:for "#fog-of-war-mode-reveil"
                  :style {:margin-right "0.5em"}} "reveil"]
         [:input#fog-of-war-mode-reveil
          {:type :radio
           :name "fog-of-war-mode"
           :checked (= :reveil @(rf/subscribe [:fog-of-war-mode]))
           :on-change #(rf/dispatch [:set-fog-of-war-mode :reveil])}]

         [:label {:for "#fog-of-war-mode-obscure"
                  :style {:margin-right "0.5em"
                          :margin-left "1em"}} "obscure"]
         [:input#fog-of-war-mode-obscure
          {:type :radio
           :name "obscure"
           :checked (= :obscure @(rf/subscribe [:fog-of-war-mode]))
           :on-change #(rf/dispatch [:set-fog-of-war-mode :obscure])}]]])]))

(defn <session-new>
  [{:keys [session-id]}]
  (fn []
    [:div#session-new.flex-rows
     [:p "player link: "
      (let [link (str (assoc-in (browser/current-uri)
                                [:query "join-session-id"]
                                @(rf/subscribe [:session-id])))]
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
      [<map> {:style {:width "100%"}}
       {:dm?                (rf/subscribe [:dm?])
        :fog-of-war-mode    (rf/subscribe [:fog-of-war-mode])
        :map-img-url        (rf/subscribe [:map-img-url])
        :map-img-alt        (rf/subscribe [:map-img-alt])
        :reveiled-cells     (rf/subscribe [:reveiled-cells])
        :highlighted-cells  (rf/subscribe [:highlighted-cells])
        :highlight-overlay  (rf/subscribe [:highlight-overlay])
        :tokens             (rf/subscribe [:tokens])
        :map-height         (rf/subscribe [:map-height])
        :map-width          (rf/subscribe [:map-width])}]
      [:div.flex-rows
       {:style {:min-width "13em"
                :padding-left "7px"}}
       [<map-definition-input>]
       [<token-list> {:dm?         @(rf/subscribe [:dm?])
                      :tokens      @(rf/subscribe [:tokens])
                      :token-count @(rf/subscribe [:token-count])}]]]
     [:pre (str "session id: " @(rf/subscribe [:session-id]))]]))


(defn <session-join>
  [{:keys [session-id]}]
  [:div
   [:p "Session " @(rf/subscribe [:session-id])]
   [:div.flex-cols
    [<map> {:style {:width "100%"}}]
    [:div.flex-rows
     {:style {:min-width "13em"
              :padding-left "7px"}}
     ;[<map-definition-input>]
     [<token-list> {:dm?         @(rf/subscribe [:dm?])
                    :tokens      @(rf/subscribe [:tokens])
                    :token-count @(rf/subscribe [:token-count])}]]]])
