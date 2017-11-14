(ns jarkeeper.views.project
  (:require [clojure.string :as string]
            [jarkeeper.views.common :as common-views]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.util :refer [escape-html]]))

(defn- render-deps [deps]
  (for [dep deps]
    [:tr
     [:td (first dep)]
     [:td (second dep)]
     [:td (:version-string (last dep))]
     [:td.status-column
       (if (nil? (last dep))
         [:span.status.up-to-date {:title "Up to date"}]
         [:span.status.out-of-date {:title "Out of date"}])]]))

(defn- render-stats [stats]
  [:section.summary.row
   [:ul
    [:li.small-12.large-4.columns
     [:span.number (:total stats)]
     [:span.stats-label "dependencies"]]
    [:li.small-12.large-4.columns
     [:span.status.up-to-date]
     [:span.number (:up-to-date stats)]
     [:span.stats-label "up to date"]]
    [:li.small-12.large-4.columns
     [:span.status.out-of-date]
     [:span.number (:out-of-date stats)]
     [:span.stats-label "out of date"]]]])

(defn- render-table [header items]
  [:table.small-12.columns
    [:thead
     [:tr
      [:th header]
      [:th {:width "180"} "Current"]
      [:th {:width "180"} "Latest"]
      [:th {:width "90"} ""]]]
   (render-deps items)])

(defn head [project]
  [:head
     [:title (str "Jarkeeper: " (:name project))]
     (common-views/common-head)
     (common-views/ga)
     (include-css "https://cdn.jarkeeper.com/app.css")])

(defn content [project]
  [:article.project-content
        [:header.row
         [:h1
           [:a {:href (:github-url project)} (:name project)]
           [:span.version (:version project)]]
         [:h2 (:description project)]
         [:div.badges
           [:img {:src (str "/" (:repo-owner project)  "/" (:repo-name project) "/downloads.svg") :alt "Downloads"}]
           (if (> (:out-of-date (:stats project)) 0)
             [:img {:src "https://cdn.jarkeeper.com/images/out-of-date.svg" :alt "Outdated dependencies"}]
             [:img {:src "https://cdn.jarkeeper.com/images/up-to-date.svg" :alt "Up to date dependencies"}])]]
        [:section.dependencies.row
          (render-stats (:stats project))
          (render-table "Dependency" (:deps project))
          (if (> (count (:plugins project)) 0)
            (html
              (render-stats (:plugins-stats project))
              (render-table "Plugin" (:plugins project))))
         (for [profile (:profiles project)]
           (if (first profile)
             (html
               (render-stats (nth profile 2))
               (render-table (name (first profile)) (second profile)))))]

       [:section.installation-instructions.row
        [:h2 "Markdown with SVG image"]
        [:code
           (str "[![Dependencies Status]"
                "(https://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "/status.svg)](https://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                ")")]
        [:h2 "HTML with SVG image"]
        [:code
           (escape-html
             (str "<a href=\""
                "https://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "\" title=\"Dependencies status\"><img src=\"https://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "/status.svg\"></a>"))]]
      [:section.installation-instructions.row
       [:h2 "Markdown with PNG image"]
       [:code
          (str "[![Dependencies Status]"
               "(https://jarkeeper.com/"
               (:repo-owner project)
               "/"
               (:repo-name project)
               "/status.png)](https://jarkeeper.com/"
               (:repo-owner project)
               "/"
               (:repo-name project)
               ")")]
       [:h2 "HTML with PNG image"]
       [:code
          (escape-html
            (str "<a href=\""
               "https://jarkeeper.com/"
               (:repo-owner project)
               "/"
               (:repo-name project)
               "\" title=\"Dependencies status\"><img src=\"https://jarkeeper.com/"
               (:repo-owner project)
               "/"
               (:repo-name project)
               "/status.png\"></a>"))]]
       [:section.installation-instructions.row
        [:h2 "Clojars downloads badge - Markdown with SVG image"]
        [:code
           (str "[![Downloads]"
                "(https://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "/downloads.svg)](https://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                ")")]
        [:h2 "Clojars downloads badge - HTML with SVG image"]
        [:code
           (escape-html
             (str "<a href=\""
                "https://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "\" title=\"Downloads\"><img src=\"https://jarkeeper.com/"
                (:repo-owner project)
                "/"
                (:repo-name project)
                "/downloads.svg\"></a>"))]]
                ])

(defn index [project]
  (html5 {:lang "en"}
    (head project)
    [:body
      (common-views/header)
      (content project)
      (common-views/common-footer)]))
