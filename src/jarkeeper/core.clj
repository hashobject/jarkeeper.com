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
  (let [[_ project-name version & info] (read-project-clj repo-owner repo-name)]
  (log/info info)
    (apply hash-map info)))


(defn project-deps [repo-owner repo-name]
  (:dependencies (project-map repo-owner repo-name)))


(project-deps "hashobject" "translate")


(defn check-deps [deps]
  (map (fn [dep]
         (conj dep (anc/artifact-outdated? dep))
         ) deps))

(check-deps (project-deps "hashobject" "translate"))


(defroutes app-routes
  (GET "/" []
       "Welcome to jarkeeper!")
  (GET "/:repo-owner/:repo-name" [repo-owner repo-name]
    (let [github-url (str "https://github.com/" repo-owner "/" repo-name)
          project-def (read-project-clj repo-owner repo-name)
          deps (check-deps (project-deps repo-owner repo-name))
          project {:name (second project-def)
                   :version (nth project-def 2)
                   :github-url github-url
                   :description (nth project-def 4)
                   :deps deps}]
       (log/info "project-def" project)
       (project-view/index project))))


(def app
  (handler/site
    app-routes))

(defn -main [port] (run-jetty app {:port (Integer. port)}))