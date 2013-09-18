(ns jarkeeper.views.project
  (:require [clojure.string :as string]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))


(defn index [project]
  (html5 {:lang "en"}
    [:head
     (include-css "/app.css")]
    [:body
      [:header.row
       [:strong (:name project)]
       [:span (:version project)]
       [:p (:description project)]]
      [:section.summary.row
       [:ul
        [:li (:total (:stats project)) " dependecies"]
        [:li (:up-to-date (:stats project)) " up to date"]
        [:li (:out-of-date (:stats project)) " out of date"]
        ]]
      [:table
        [:thead
         [:tr
          [:th "Dependecy"]
          [:th "Used"]
          [:th "Latest"]]]
        (for [dep (:deps project)]
            [:tr
             [:td (first dep)]
             [:td (second dep)]
             [:td (str (first (last dep)))]])]


     ]))
