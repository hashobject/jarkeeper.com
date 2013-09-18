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
            [jarkeeper.views.project :as project-view]
            [ancient-clj.core :as anc])
  (:import (java.io PushbackReader)))


(defn read-project-clj [repo-owner repo-name]
  (let [url (str "https://raw.github.com/" repo-owner "/" repo-name "/master/project.clj")]
    (edn/read
        (PushbackReader.
          (io/reader url)))))




(defn project-map [repo-owner repo-name]
  (let [github-url (str "https://github.com/" repo-owner "/" repo-name)
        [_ project-name version & info] (read-project-clj repo-owner repo-name)
        info-map (apply hash-map info)
        result (assoc info-map :name project-name :version version :github-url github-url)]
       (log/info result)
       result))


(defn check-deps [deps]
  (map (fn [dep]
         (conj dep (anc/artifact-outdated? dep))
         ) deps))


(defroutes app-routes
  (GET "/" []
       "Welcome to jarkeeper!")
  (GET "/:repo-owner/:repo-name" [repo-owner repo-name]
    (let [project-def (project-map repo-owner repo-name)
          deps (check-deps (:dependencies project-def))
          project (assoc project-def :deps deps)]
       (log/info "project-def" project)
       (project-view/index project))))


(def app
  (handler/site
    app-routes))

(defn -main [port] (run-jetty app {:port (Integer. port)}))
