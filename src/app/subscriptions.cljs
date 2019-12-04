(ns app.subscriptions
  (:require
    [re-frame.core :as rf]
    [app.views :as v]
    ))

; Query
(rf/reg-sub
  :active-view
  (fn [db _query-vec]
    (get {:start        v/<home>
          :session-new  v/<dm-view>
          :session-join v/<player-view>}
         (:active-view-id db))))

(rf/reg-sub
  :session-id
  (fn [db _query-vec]
    (-> db :session-id)))

(rf/reg-sub
  :token-count
  (fn [db _query-vec]
    (-> db :players count)))

(rf/reg-sub
  :tokens
  (fn [db _query-vec]
    (-> db :players)))

(rf/reg-sub
  :highlight-overlay
  (fn [db _query-vec]
    (-> db :highlight-overlay)))

(rf/reg-sub
  :map-width
  (fn [db _query-vec]
    (some-> db :map :width)))

(rf/reg-sub
  :map-height
  (fn [db _query-vec]
    (some-> db :map :height)))

(rf/reg-sub :map-pad-left   (fn [db _query-vec] (some-> db :map :padding :left)))
(rf/reg-sub :map-pad-right  (fn [db _query-vec] (some-> db :map :padding :right)))
(rf/reg-sub :map-pad-top    (fn [db _query-vec] (some-> db :map :padding :top)))
(rf/reg-sub :map-pad-bottom (fn [db _query-vec] (some-> db :map :padding :bottom)))

(rf/reg-sub
  :map-img-url
  (fn [db _query-vec]
    (some-> db :map :img-url)))

(rf/reg-sub
  :map-img-alt
  (fn [db _query-vec]
    (some-> db :map :img-alt)))

(rf/reg-sub
  :dm?
  (fn [db _query-vec]
    (some-> db :dm?)))

(rf/reg-sub
  :reveiled-cells
  (fn [db _query-vec]
    (some-> db :reveiled-cells)))

(rf/reg-sub
  :fog-of-war-mode
  (fn [db _query-vec]
    (some-> db :fog-of-war-mode)))


(rf/reg-sub
  :highlighted-cells
  (fn [db _query-vec]
    (some-> db :highlighted-cells)))


