(ns jarkeeper.core
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [ring.middleware.resource :refer [wrap-resource]]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [jarkeeper.views.project :as project-view])
  (:import (java.io PushbackReader)))


(defn read-project-clj [repo-owner repo-name]
  (let [url (str "https://raw.github.com/" repo-owner "/" repo-name "/master/project.clj")]
    (edn/read
        (PushbackReader.
          (io/reader url)))))

(defroutes app-routes
  (GET "/" []
       "Welcome to jarkeeper!")
  (GET "/:repo-owner/:repo-name" [repo-owner repo-name]
    (let [github-url (str "https://github.url/" repo-owner "/" repo-name)
          project-def (read-project-clj repo-owner repo-name)
          project {:name (second project-def)
                   :version (nth project-def 2)
                   :github-url github-url
                   :description (nth project-def 4)}]
       (project-view/index project))))


(def app
  (handler/site
    app-routes))


(defn -main [port] (run-jetty app {:port (Integer. port)}))