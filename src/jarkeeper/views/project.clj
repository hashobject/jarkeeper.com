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
        [:li.small-12.large-4.columns
         [:span.number (:total (:stats project))]
         [:span.stats-label "dependencies"]]
        [:li.small-12.large-4.columns
         [:span.status.up-to-date]
         [:span.number (:up-to-date (:stats project))]
         [:span.stats-label "up to date"]]
        [:li.small-12.large-4.columns
         [:span.status.out-of-date]
         [:span.number (:out-of-date (:stats project))]
         [:span.stats-label "out of date"]]
        ]]
      [:section.dependencies.row
        [:table.small-12.columns
          [:thead
           [:tr
            [:th "Dependency"]
            [:th "Used"]
            [:th "Latest"]
            [:th {:width "90"} "Status"]
            ]]
          (for [dep (:deps project)]
              [:tr
               [:td (first dep)]
               [:td (second dep)]
               [:td (str (first (last dep)))]
               [:td.status-column
                 (if (nil? (last dep))
                   [:span.status.up-to-date {:title "Up to date"}]
                   [:span.status.out-of-date {:title "Out of date"}])]])]]

     [:section.installation-instructions.row
      [:h1 "Markdown"]
      [:code
         (str "[![Dependencies Status]"
              "(http://jarkeeper.com/"
              (:owner project)
              "/"
              (:name project)
              "/status.png)](http://jarkeeper.com/"
              (:owner project)
              "/"
              (:name project)
              ")")]]
     (common-views/common-footer)
     ]))
