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
            [jarkeeper.views.index :as index-view]
            [jarkeeper.views.project :as project-view]
            [ancient-clj.core :as anc])
  (:import (java.io PushbackReader)))


(defn read-project-clj [repo-owner repo-name]
  (let [url (str "https://raw.github.com/" repo-owner "/" repo-name "/master/project.clj")]
    (edn/read
        (PushbackReader.
          (io/reader url)))))

(defn check-deps [deps]
  (map (fn [dep]
         (conj dep (anc/artifact-outdated? dep))
         ) deps))


(defn calculate-stats [deps]
  (let [up-to-date-deps (remove nil? (map (fn [dep] (if (nil? (last dep)) dep nil)) deps))
        out-of-date-deps (remove nil? (map (fn [dep] (if (nil? (last dep)) nil dep)) deps))
        stats {:total (count deps)
               :up-to-date (count up-to-date-deps)
               :out-of-date (count out-of-date-deps)}]
    stats))

(defn project-map [repo-owner repo-name]
  (let [github-url (str "https://github.com/" repo-owner "/" repo-name)
        [_ project-name version & info] (read-project-clj repo-owner repo-name)
        info-map (apply hash-map info)
        deps (check-deps (:dependencies info-map))
        stats (calculate-stats deps)
        result (assoc info-map
                 :name project-name
                 :owner repo-owner
                 :version version
                 :github-url github-url
                 :deps deps
                 :stats stats)]
       (log/info result)
       result))


(defn- repo-redirect [{:keys [params]}]
  (log/info params)
  (resp/redirect (str "/" (:repo-url params))))

(defroutes app-routes
  (GET "/" []
       (index-view/index))

  (POST "/find" [] repo-redirect)

  (GET "/:repo-owner/:repo-name" [repo-owner repo-name]
    (let [project (project-map repo-owner repo-name)]
       (log/info "project-def" project)
       (project-view/index project))))


(def app
  (-> #'app-routes
     (wrap-resource "public")
     (wrap-base-url)
     (handler/api)))

(defn -main [port] (run-jetty app {:port (Integer. port)}))
