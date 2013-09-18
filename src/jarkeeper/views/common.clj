(ns jarkeeper.views.common
  (:use [hiccup.core :only (html)]
        [hiccup.page :only (html5 include-css include-js)])
  (:require [clojure.string :as string]))

(defn common-head [title]
  '([:meta {:charset "utf-8"}]
    [:title (str "Jarkeeper: " title)]
    [:meta {:name "description" :content "Identifies out of date dependencies for Clojure projects on GitHub"}]
    [:meta {:name "keywords" :content (str title ", clojure, dependecies, version, up to date version, out of date version")}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, user-scalable=no"}]
    [:meta {:name "author" :content "Hashobject (team@hashobject.com)"}]
    [:link {:rel "shortcut icon" :href "/favicon.ico"}]))


(defn common-footer []
  [:footer.row
   [:div.small-12.columns
    [:p "Brought to you by "
      [:a {:href "http://hashobject.com/"} "Hashobject team"] "."]
    [:p
     [:a {:href "http://github.com/hashobject/jarkeeper.com"} "Jarkeeper"]
     " is open source project hosted on GitHub."]]])
