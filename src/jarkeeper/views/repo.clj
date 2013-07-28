(ns jarkeeper.views.repo
  (:require [clojure.string :as string]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]))


(defn index [repo]
  (html5 {:lang "uk"}
    [:body
      "Hello"]))