(ns app.views
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [app.browser :as browser]
    [app.local-storage :as local-storage]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [app.view.components :refer [<token-list> <map>
                                 <map-definition-input>
                                 <initiative-list>
                                 <collapsable>
                                 <btn> <btn-group>]]))

(defn <host-pinger>
  []
  (let [timer (atom nil) ]
    (r/create-class
      {:display-name "host-pinger"

       :component-did-mount
       (fn [this]
         (prn ::component-did-mount)
         (reset! timer
                 (js/window.setInterval
                   #(rf/dispatch
                      [:ping-host {:request-ts-ms (.now js/Date)}])
                   5000)))

       :component-will-unmount
       (fn [this]
         (prn ::component-will-unmount)
         (swap! timer
                (fn [timer-id]
                  (js/window.clearInterval timer-id))))

       :reagent-render
       (fn [] )})))

(defn <ping>
  []
  (let [now @(rf/subscribe [:now-ts-ms])
        last-pong @(rf/subscribe [:last-pong])
        dt  (- now
               (or (:timestamp-ms last-pong) 0))
        ok? (< dt 10000)]
    [:div.host-ping
     (if ok?
       [:span.host-ping--ok
        " "]
       [:span.host-ping--issue
        (str "no response from DM for " (int (/ dt 1000)) " seconds")]
       )]))

(defn <home>
  []
  (let [new-session-id (r/atom (-> (Math/random)
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
                [:p (:session-id k)]
                [:button.btn
                 {:on-click #(local-storage/remove! k)
                  :style {:display :inline-block}
                  :key (str "delete_sess_" (:session-id k))}
                 (str "Delete!")]
                [:button.btn
                 {:on-click #(go
                               (rf/dispatch [:host-session (:session-id k)])
                               (rf/dispatch [:state-init (local-storage/get k)]))
                  :style {:display :inline-block}
                  :key (str "restore_sess_" (:session-id k))}
                 (str "Restore >>")]]))
           [:div
            [:input
             {:value @new-session-id
              :on-change #(reset! new-session-id (some-> % .-target .-value))}]

            [:button.btn
             {:on-click #(rf/dispatch [:host-session @new-session-id])}
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
  [:p.player-link
   {:key (str "player-link")}
   "player link: "
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
    [:<>
     
     [:h1.dm-view--title "D&D Mapper"]
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
           :token-count (rf/subscribe [:token-count])}])]]]))


(defn <player-view>
  [{:keys []}]
  [:<>
   [:div.player-view--header
   [<initiative-list> {:dm?    (rf/subscribe [:dm?])
                       :tokens (rf/subscribe [:tokens])}]]
   [<host-pinger>]
   [<ping>]
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
    [:p "Session " @(rf/subscribe [:session-id])]]])
