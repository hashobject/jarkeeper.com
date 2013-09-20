(ns jarkeeper.views.common
  (:use [hiccup.core :only (html)]
        [hiccup.page :only (html5 include-css include-js)])
  (:require [clojure.string :as string]))

(defn common-head []
  '([:meta {:charset "utf-8"}]
    [:meta {:name "description" :content "Identifies out of date dependencies for Clojure projects hosted on GitHub"}]
    [:meta {:name "keywords" :content "clojure, dependecies, version, up to date version, out of date version"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, user-scalable=no"}]
    [:meta {:name "author" :content "Hashobject (team@hashobject.com)"}]
    [:meta {:rel "author" :type "text/plain" :href "/humans.txt"}]
    [:link {:rel "shortcut icon" :href "/favicon.ico"}]))


(defn common-footer []
  [:footer.footer.row
   [:div.small-12.columns
    [:p "Brought to you by "
      [:a {:href "http://hashobject.com/"} "Hashobject team"] "."]
    [:p
     [:a {:href "http://github.com/hashobject/jarkeeper.com"} "Jarkeeper"]
     " is open source project hosted on GitHub."]]])


(defn ga []
  [:script "
    (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

    ga('create', 'UA-44209834-1', 'jarkeeper.com');
    ga('send', 'pageview');
   "])


(defn header []
  [:header.header.row
   [:div
    [:a {:href "http://www.jarkeeper.com"}[:div.logo]]]])
