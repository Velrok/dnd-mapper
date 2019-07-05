(ns app.views
  (:require
    [app.browser :as browser]
    [app.local-storage :as local-storage]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [app.websocket-io :as ws]
    [app.state :as state]
    [app.view.components :refer [<token-list> <map> <map-definition-input>
                                 <btn> <btn-group>]]))

(defn <start>
  []
  (let [new-session-name (r/atom (-> (Math/random)
                                     (* 100000000)
                                     int
                                     str))]
    (fn []
      (let [join-session-id (get-in (browser/current-uri) [:query "join-session-id"])]
        (if-not join-session-id
          [:div
           [:h2 (str "start a session")]
           (doall
             (for [k (filter :dm? (local-storage/keys))]
               [:button.btn
                {:on-click #(rf/dispatch [:state-init (local-storage/get k)])
                 :style {:display :block}
                 :key (str "restore_sess_" (:session-id k))}
                (str "Restore " (:session-id k) " >>")]))
           [:div
            [:input
             {:value @new-session-name
              :on-change #(reset! new-session-name (some-> % .-target .-value))}]

            [:button.btn
             {:on-click #(rf/dispatch [:host-session @new-session-name])}
             "create new session >>"]]
           ]

          [:div
           [:h2 "join"]
           [:button.btn
            {:on-click #(rf/dispatch [:join-session join-session-id])}
            (str "join session "  join-session-id " >>")]])))))

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
      [<map>
       {:style {:width "100%"}}
       {:dm?                (rf/subscribe [:dm?])
        :fog-of-war-mode    (rf/subscribe [:fog-of-war-mode])
        :map-img-url        (rf/subscribe [:map-img-url])
        :map-img-alt        (rf/subscribe [:map-img-alt])
        :reveiled-cells     (rf/subscribe [:reveiled-cells])
        :highlighted-cells  (rf/subscribe [:highlighted-cells])
        :highlight-overlay  (rf/subscribe [:highlight-overlay])
        :tokens             (rf/subscribe [:tokens])
        :map-pad-left       (rf/subscribe [:map-pad-left])
        :map-pad-right      (rf/subscribe [:map-pad-right])
        :map-pad-top        (rf/subscribe [:map-pad-top])
        :map-pad-bottom     (rf/subscribe [:map-pad-bottom])
        :map-height         (rf/subscribe [:map-height])
        :map-width          (rf/subscribe [:map-width])}]

      [:div.flex-rows
       {:style {:min-width "13em"
                :padding-left "7px"}}
       [<map-definition-input>
        {}
        {:map-img-url       (rf/subscribe [:map-img-url])
         :map-width         (rf/subscribe [:map-width])
         :map-height        (rf/subscribe [:map-height])
         :map-pad-left      (rf/subscribe [:map-pad-left])
         :map-pad-right     (rf/subscribe [:map-pad-right])
         :map-pad-top       (rf/subscribe [:map-pad-top])
         :map-pad-bottom    (rf/subscribe [:map-pad-bottom])
         :highlight-overlay (rf/subscribe [:highlight-overlay])
         :dm?               (rf/subscribe [:dm?])
         :fog-of-war-mode   (rf/subscribe [:fog-of-war-mode])}]
       [<token-list>
        {}
        {:dm?         (rf/subscribe [:dm?])
         :tokens      (rf/subscribe [:tokens])
         :token-count (rf/subscribe [:token-count])}]]]
     [:pre (str "session id: " @(rf/subscribe [:session-id]))]]))


(defn <session-join>
  [{:keys [session-id]}]
  [:div
   [:p "Session " @(rf/subscribe [:session-id])]
   [:div.flex-cols
    [<map>
     {:style {:width "100%"}}
     {:dm?                (rf/subscribe [:dm?])
      :fog-of-war-mode    (rf/subscribe [:fog-of-war-mode])
      :map-img-url        (rf/subscribe [:map-img-url])
      :map-img-alt        (rf/subscribe [:map-img-alt])
      :reveiled-cells     (rf/subscribe [:reveiled-cells])
      :highlighted-cells  (rf/subscribe [:highlighted-cells])
      :highlight-overlay  (rf/subscribe [:highlight-overlay])
      :tokens             (rf/subscribe [:tokens])
      :map-pad-left       (rf/subscribe [:map-pad-left])
      :map-pad-right      (rf/subscribe [:map-pad-right])
      :map-pad-top        (rf/subscribe [:map-pad-top])
      :map-pad-bottom     (rf/subscribe [:map-pad-bottom])
      :map-height         (rf/subscribe [:map-height])
      :map-width          (rf/subscribe [:map-width])}]
    [:div.flex-rows
     {:style {:min-width "13em"
              :padding-left "7px"}}
     [<token-list>
      {}
      {:dm?         (rf/subscribe [:dm?])
       :tokens      (rf/subscribe [:tokens])
       :token-count (rf/subscribe [:token-count])}]]]])
