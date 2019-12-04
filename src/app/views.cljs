(ns app.views
  (:require
    [app.browser :as browser]
    [app.local-storage :as local-storage]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [app.view.components :refer [<token-list> <map>
                                 <map-definition-input>
                                 <collapsable>
                                 <btn> <btn-group>]]))

(defn <home>
  []
  (let [new-session-name (r/atom (-> (Math/random)
                                     (* 100000000)
                                     int
                                     str))]
    (fn []
      (let [join-session-id (get-in (browser/current-uri) [:query "join-session-id"])]
        (if-not join-session-id
          [:<>
           [:h1.app--title "D&D Mapper"]
           [:h2 (str "start a session")]
           (doall
             (for [k (filter :dm? (local-storage/keys))]
               [:div
                [:button.btn
                 {:on-click #(local-storage/remove! k)
                  :style {:display :inline-block}
                  :key (str "delete_sess_" (:session-id k))}
                 (str "Delete " (:session-id k) "!")]
                [:button.btn
                 {:on-click #(rf/dispatch [:state-init (local-storage/get k)])
                  :style {:display :inline-block}
                  :key (str "restore_sess_" (:session-id k))}
                 (str "Restore " (:session-id k) " >>")]]))
           [:div
            [:input
             {:value @new-session-name
              :on-change #(reset! new-session-name (some-> % .-target .-value))}]

            [:button.btn
             {:on-click #(rf/dispatch [:host-session @new-session-name])}
             "create new session >>"]]
           ]

          [:<>
           [:h1.app--title "D&D Mapper"]
           [:h2 "join"]
           [:button.btn
            {:on-click #(rf/dispatch [:join-session join-session-id])}
            (str "join session "  join-session-id " >>")]])))))

(defn- <player-link>
  []
  [:p.player-link "player link: "
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
         (js/alert "player link copied"))}])])

(defn- <session-id>
  []
  [:pre.session-id (str "session id: " @(rf/subscribe [:session-id]))])

(defn <dm-view>
  [{:keys []}]
  (fn []
    [:div#dm-view
     ;[:h1.app--title__mini "D&D Mapper"]
     [:div.dm-view--map
      {}
      [<map>
       {}
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
        :map-width          (rf/subscribe [:map-width])}]]
     [:div.dm-view--controlls

      [<collapsable> {:title "setup"}
       [<player-link>]
       [<session-id>]
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
         :fog-of-war-mode   (rf/subscribe [:fog-of-war-mode])}]]
      (when @(rf/subscribe [:dm?])
        [<token-list>
         {}
         {:dm?         (rf/subscribe [:dm?])
          :tokens      (rf/subscribe [:tokens])
          :token-count (rf/subscribe [:token-count])}])]]))


(defn <player-view>
  [{:keys []}]
  [:div#player-view
   [<map>
     {}
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
   [:p "Session " @(rf/subscribe [:session-id])]])
