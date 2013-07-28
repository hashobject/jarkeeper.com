(ns jarkeeper.core
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [ring.middleware.resource :refer [wrap-resource]]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]
            [jarkeeper.views.repo :as repo-view]))

(defroutes app-routes
  (GET "/" []
       "Welcome to jarkeeper!")
  (GET "/:repo-owner/:repo-name" [repo-owner repo-name]
       (repo-view/index {})))


(def app
  (handler/site
    app-routes))


(defn -main [port] (run-jetty app {:port (Integer. port)}))