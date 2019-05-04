(ns app.state
  (:require [reagent.core :as r]))

(defonce fog-of-war-mode (r/atom :reveil)) ; :obscure

(defonce reveiled-cells (r/atom #{}))
