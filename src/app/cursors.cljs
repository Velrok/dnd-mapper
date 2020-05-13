(ns app.cursors
  (:refer-clojure :exclude [map])
  (:require
    [app.state :as state]
    [reagent.core :as r]))

(defn token
  [id]
  (r/cursor state/shared [:tokens id]))

(defn tokens
  []
  (r/cursor state/shared [:tokens]))

(defn token-position
  [id]
  (r/cursor state/shared [:tokens id :position]))

(defn map
  []
  (r/cursor state/shared [:map]))

(defn map-cols
  []
  (r/cursor state/shared [:map :columns]))

(defn map-rows
  []
  (r/cursor state/shared [:map :rows]))

(defn map-img-url
  []
  (r/cursor state/shared [:map :img-url]))

(defn reveiled-cells
  []
  (r/cursor state/shared [:map :reveiled-cells]))
