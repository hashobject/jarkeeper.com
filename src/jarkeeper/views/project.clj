(ns jarkeeper.views.project
  (:require [clojure.string :as string]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))


(defn index [project]
  (html5 {:lang "en"}
    [:body
      [:header
       [:strong (:name project)]
       [:span (:version project)]
       [:p (:description project)]]]))