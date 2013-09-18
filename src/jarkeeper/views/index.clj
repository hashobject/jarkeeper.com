(ns jarkeeper.views.index
  (:require [clojure.string :as string]
            [jarkeeper.views.common :as common-views]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.util :refer [escape-html]]))


(defn index []
  (html5 {:lang "en"}
    [:head
     [:title "Jarkeeper: identify out of date dependecies!" ]
     (common-views/common-head)
     (include-css "/app.css")]
    [:body
      [:article.project-content
       [:form.row {:method "GET" :action "find"}
        [:div.row
         [:div.large-12.columns
          [:input.input {:name "repo-url" :placeholder "e.x. hashobject/mandrill"}]]]
        [:div.row
         [:div.large-12.columns
          [:button "Check!"]]]]
      ]
     (common-views/common-footer)
    ]))
