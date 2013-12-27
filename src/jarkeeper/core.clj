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



(defn- starting-num? [string]
  (number? (read-string (str (first (name string))))))


(defn read-project-clj [repo-owner repo-name]
  (let [url (str "https://raw.github.com/" repo-owner "/" repo-name "/master/project.clj")]
    (edn/read
        (PushbackReader.
          (io/reader url)))))


(defn check-deps [deps]
  (map (fn [dep]
         (conj dep (anc/artifact-outdated? {:snapshots? false :qualified? false} dep))
         ) deps))

(defn calculate-stats [deps]
  (let [up-to-date-deps (remove nil? (map (fn [dep] (if (nil? (last dep)) dep nil)) deps))
        out-of-date-deps (remove nil? (map (fn [dep] (if (nil? (last dep)) nil dep)) deps))
        stats {:total (count deps)
               :up-to-date (count up-to-date-deps)
               :out-of-date (count out-of-date-deps)}]
    stats))

(defn check-profiles [profiles]
  (map (fn [profile-entry]
         (let [profile (val profile-entry)
               profile-name (key profile-entry)]
               (if (not (starting-num? profile-name))
                 (if-let [dependencies (:dependencies profile)]
                   [profile-name (check-deps dependencies) (calculate-stats dependencies)]))))
       profiles))


(defn project-map [repo-owner repo-name]
  (let [github-url (str "https://github.com/" repo-owner "/" repo-name)
        [_ project-name version & info] (read-project-clj repo-owner repo-name)
        info-map (apply hash-map info)
        deps (check-deps (:dependencies info-map))
        profiles (check-profiles (:profiles info-map))
        stats (calculate-stats deps)
        result (assoc info-map
                 :name project-name
                 :repo-name repo-name
                 :repo-owner repo-owner
                 :version version
                 :github-url github-url
                 :deps deps
                 :profiles profiles
                 :stats stats)]
       (log/info "project map" result profiles)
       result))

(defn- repo-redirect [{:keys [params]}]
  (log/info params)
  (resp/redirect (str "/" (:repo-url params))))

(defroutes app-routes
  (GET "/" []
       (index-view/index))

  (POST "/find" [] repo-redirect)

  (GET "/:repo-owner/:repo-name" [repo-owner repo-name]
    (try
      (let [project (project-map repo-owner repo-name)]
           (log/info "project-def" project)
           (project-view/index project))
      (catch Exception e
        (resp/redirect "/"))))

  (GET "/:repo-owner/:repo-name/status.png" [repo-owner repo-name]
    (try
       (let [project (project-map repo-owner repo-name)
            out-of-date-count (:out-of-date (:stats project))]
         (if (> out-of-date-count 0)
           (resp/file-response "resources/public/images/out-of-date.png")
           (resp/file-response "resources/public/images/up-to-date.png")))
      (catch Exception e {:status 404}))))


(def app
  (-> #'app-routes
     (wrap-resource "public")
     (wrap-base-url)
     (handler/api)))

(defn -main [port] (run-jetty app {:port (Integer. port)}))
