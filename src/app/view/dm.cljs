(ns app.view.dm
  (:require
    [app.cursors :as cursors]
    [reagent.core :as r]
    [app.browser :as browser]
    [app.local-storage :as local-storage]
    [app.websocket-io :as ws]
    [app.state :as state]
    [app.view.components :refer [<app-title>
                                 <side-draw>
                                 <websocket-status>
                                 <btn>
                                 <token-card>
                                 <token-card-mini>
                                 <input>
                                 <container>
                                 <map-svg>]]))

(defn <dm-view>
  []
  (let [selected-token (r/atom nil)]
    @state/report-state-diffs
    @state/persist-state-changes
    (swap! state/local assoc :dm? true)
    (when-let [local-init (local-storage/get
                            {:dm? (:dm? @state/local)
                             :session-id (browser/session-id)})]
      (reset! state/shared local-init))
    (fn []
      (let [columns     (cursors/map-cols)
            rows        (cursors/map-rows)
            map-url     (cursors/map-img-url)
            session-id  (r/track browser/session-id)
            ws-state    @ws/ready-state]
        [:<>
         [<side-draw>
          {}
          [<container>
           {:title "map settings"}

           [<input> {:label "player link"
                     :value (str "./join?session=" session-id)}]

           [<input> {:label "columns"
                     :type "number"
                     :min 1
                     :value @columns
                     :on-change #(reset! columns (int %))}]

           [<input> {:label "rows"
                     :type "number"
                     :min 1
                     :value @rows
                     :on-change #(reset! rows (int %))}]

           [<input> {:label "map url"
                     :value @map-url
                     :on-change #(reset! map-url %)}]]

          [<container>
           {:title "tokens"}
           [(fn [& _]
              [:<>
               (for [t (or (some->> @(cursors/tokens)
                                    vals
                                    (sort-by :initiative)
                                    reverse)
                           [])]
                 (with-meta
                   [<token-card> {:id (:id t)}
                    [<btn> {:on-click #(swap! (cursors/tokens) dissoc (:id t))
                            :color "error"}
                     "remove"]]
                   {:key (:id t)}))])]
           (let [make-new-token-value (fn []
                                        {:id (-> (Math/random)
                                                 (* 1000000)
                                                 str)
                                         :name "new monster"
                                         :initiative 5
                                         :img-url "https://i.imgur.com/kCysnYk.png"
                                         :hp 1
                                         :max-hp 1})
                 new-token (r/atom (make-new-token-value))]
             [<token-card> {:key (:id @new-token)
                            :token-ref new-token}
              [<btn> {:on-click #(do (swap! (cursors/tokens) assoc (:id @new-token) @new-token)
                                     (reset! new-token (make-new-token-value)))
                      :color "primary"}
               "add"]])]]
         [<app-title>]
         [:<>
          [<websocket-status>
           {:ready-state ws-state
            :style {:position :fixed
                    :right "0.5rem"
                    :bottom "0.5rem"}
            :on-click ws/ping!}]]
         [<map-svg>
          {:overlay-opacity 0.5
           :on-token-click #(do
                              (prn [::on-token-click %])
                              (reset! selected-token %))}]
         (when @selected-token
           [:div.dm-view__token-quick-change
            [(<token-card-mini> {:id (:id @selected-token)
                                 :on-close #(reset! selected-token nil)})]])]))))
