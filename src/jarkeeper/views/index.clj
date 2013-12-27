(ns jarkeeper.views.index
  (:require [clojure.string :as string]
            [jarkeeper.views.common :as common-views]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.util :refer [escape-html]]))


(defn index []
  (html5 {:lang "en"}
    [:head
     [:title "Jarkeeper: identify out of date dependecies!"]
     (common-views/common-head)
     (common-views/ga)
     (include-css "/app.css")]
    [:body
      (common-views/header)
      [:article.index-content
       [:h1 "Jarkeeper identifies outdated dependencies in your Clojure project."]
       [:form.find-form {:method "POST" :action "find"}
        [:div.row
         [:div.small-3.columns
          [:label.right.inline {:for "repo-url"} "Repo name"]]
         [:div.small-6.columns
          [:input#repo-url {:type "text" :name "repo-url" :placeholder "e.x. korma/Korma" :autocomplete "false"}]]
         [:div.small-3.columns
           [:button "Check!"]]]]]
     (common-views/common-footer)]))
