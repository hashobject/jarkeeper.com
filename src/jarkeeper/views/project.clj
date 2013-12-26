(ns jarkeeper.views.project
  (:require [clojure.string :as string]
            [jarkeeper.views.common :as common-views]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.util :refer [escape-html]]))


(defn index [project]
  (html5 {:lang "en"}
    [:head
     [:title (str "Jarkeeper: " (:name project))]
     (common-views/common-head)
     (common-views/ga)
     (include-css "/app.css")]
    [:body
      (common-views/header)
      [:article.project-content
        [:header.row
         [:h1
           [:a {:href (:github-url project)} (:name project)]
           [:span.version (:version project)]]
         [:h2 (:description project)]
         (if (> (:out-of-date (:stats project)) 0)
           [:img {:src "/images/out-of-date.png"}]
           [:img {:src "/images/up-to-date.png"}]
           )
           ]
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
              [:th {:width "90"} "Status"]]]
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
        [:h2 "Markdown"]
        [:code
           (str "[![Dependencies Status]"
                "(http://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "/status.png)](http://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                ")")]
        [:h2 "HTML"]
        [:code
           (escape-html (str "<a href=\""
                "http://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "\" title=\"Dependencies status\"><img src=\"http://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "/status.png\"></a>"))]
        ]]
     (common-views/common-footer)
    ]))
