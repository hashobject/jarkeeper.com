(ns jarkeeper.core
  (:use
    [ring.adapter.jetty :only [run-jetty]])
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [ring.middleware.resource :refer [wrap-resource]]
            [hiccup.middleware :refer [wrap-base-url]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]))

(defroutes app-routes
  (GET "/" [] "Hello"))


(def app
  (handler/site
    app-routes))


(defn -main [port] (run-jetty app {:port (Integer. port)}))