(ns jarkeeper.views.project
  (:require [clojure.string :as string]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))


(defn index [project]
  (html5 {:lang "en"}
    [:head
     (include-css "/app.css")]
    [:body
      [:header
       [:strong (:name project)]
       [:span (:version project)]
       [:p (:description project)]]
      [:table
        [:thead
         [:tr
          [:th "Depedecy"]
          [:th "Used"]
          [:th "Latest"]]]
        (for [dep (:deps project)]
            [:tr
             [:td (first dep)]
             [:td (second dep)]
             [:td (str (last dep))]])]


     ]))
