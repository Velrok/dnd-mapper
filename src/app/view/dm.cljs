(ns app.view.dm
  (:require
    [reagent.core :as r]
    [app.browser :as browser]
    [app.local-storage :as local-storage]
    [app.websocket-io :as ws]
    [app.state :as state]
    [app.view.components :refer [<app-title>
                                 <side-draw>
                                 <websocket-status>
                                 <token-card>
                                 <token-card-mini>
                                 <input>
                                 <container>
                                 <map-svg>]]))

(defn <dm-view>
  []
  (let [selected-token-id (r/atom nil)
        selected-token    #_(r/cursor state/shared
                                    [:tokens @selected-token-id])
        (r/track #(some-> @state/shared :tokens (get @selected-token-id)))]
    @state/report-state-diffs
    @state/persist-state-changes
    (swap! state/local assoc :dm? true)
    (when-let [local-init (local-storage/get
                            {:dm? (:dm? @state/local)
                             :session-id (browser/session-id)})]
      (reset! state/shared local-init))
    (fn []
      (let [columns     (r/cursor state/shared [:map :width])
            rows        (r/cursor state/shared [:map :height])
            map-url     (r/cursor state/shared [:map :img-url])
            tokens      (r/track #(some-> @state/shared :tokens vals))
            _ @selected-token-id
            session-id  (r/track browser/session-id)
            ws-state    @ws/ready-state
            reset-token! #(do
                            (prn [::reset-token! %])
                            (swap! state/shared
                                 assoc-in
                                 [:tokens (:id %)]
                                 %))]
        [:<>
         [<side-draw>
          {}
          ^{:key (gensym "side-draw")}
          [<container>
           {:title "map settings"}

           ^{:key (gensym "map-settings-item-")}
           [<input> {:label "player link"
                     :value (str "./join?session=" session-id)}]

           ^{:key (gensym "map-settings-item-")}
           [<input> {:label "columns"
                     :type "number"
                     :min 1
                     :value @columns
                     :on-change #(reset! columns (int %))}]

           ^{:key (gensym "map-settings-item-")}
           [<input> {:label "rows"
                     :type "number"
                     :min 1
                     :value @rows
                     :on-change #(reset! rows (int %))}]

           ^{:key (gensym "map-settings-item-")}
           [<input> {:label "map url"
                     :value @map-url
                     :on-change #(reset! map-url %)}]]

          ^{:key (gensym "side-draw")}
          [<container>
           {:title "tokens"}
           (doall
             (for [t @tokens]
               [<token-card> {:token (delay t)
                              :on-change reset-token!}]))]]
         [<app-title>]
         [:<>
          [<websocket-status>
           {:ready-state ws-state
            :style {:position :fixed
                    :right "0.5rem"
                    :bottom "0.5rem"}
            :on-click ws/ping!}]]
         [<map-svg>
          {:img-url map-url
           :w columns
           :h rows
           :on-cells-reveil #(prn "reveil" %)
           :on-cells-hide #(prn "hide" %)
           :overlay-opacity 0.5
           :on-token-change reset-token!
           :on-token-click #(do
                              (reset! selected-token-id (:id %))
                              (prn [::selected-token-id @selected-token-id]))
           :tokens tokens}]
         [<token-card-mini> {:token selected-token
                             :on-change reset-token!}]]))))
