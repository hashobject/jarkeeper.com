(ns jarkeeper.views.project
  (:require [clojure.string :as string]
            [jarkeeper.views.common :as common-views]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))


(defn index [project]
  (html5 {:lang "en"}
    [:head
     (common-views/common-head (:name project))
     (include-css "/app.css")]
    [:body.project-page
      [:header.row
       [:h1
         [:a {:href (:github-url project)} (:name project)]
         [:span.version (:version project)]]
       [:h2 (:description project)]]
      [:section.summary.row
       [:ul
        [:li.small-12.large-3.columns (:total (:stats project)) " dependencies"]
        [:li.small-12.large-3.columns (:up-to-date (:stats project)) " up to date"]
        [:li.small-12.large-3.columns (:out-of-date (:stats project)) " out of date"]
        ]]
      [:section.dependencies.row
        [:table.small-12.columns
          [:thead
           [:tr
            [:th "Dependecy"]
            [:th "Used"]
            [:th "Latest"]]]
          (for [dep (:deps project)]
              [:tr
               [:td (first dep)]
               [:td (second dep)]
               [:td (str (first (last dep)))]])]]

      (common-views/common-footer)
     ]))
