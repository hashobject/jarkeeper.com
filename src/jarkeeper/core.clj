(ns jarkeeper.core
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE HEAD]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.json :refer [wrap-json-response]]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.middleware.defaults :refer :all]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [jarkeeper.downloads :as downloads]
            [jarkeeper.statuses :as statuses]
            [jarkeeper.views.index :as index-view]
            [jarkeeper.views.project :as project-view]
            [jarkeeper.views.json :as project-json]
            [environ.core :refer [env]]
            [clj-rollbar.core :as rollbar]
            [matchbox.core :as m])

  (:import (java.io PushbackReader)
           [java.text SimpleDateFormat]
           [java.util Locale TimeZone]))


(def fire-root (m/connect "https://jarkeeper.firebaseio.com"))
(if (env :firebase-token)
  (m/auth-custom fire-root (env :firebase-token) prn-str))


(def last-modified-formatter "EEE, dd MMM yyyy HH:mm:ss zzz")

(defn- ^SimpleDateFormat formatter [format]
  (doto (SimpleDateFormat. ^String format Locale/US)
    (.setTimeZone (TimeZone/getTimeZone "GMT"))))

(defn last-modified []
  (.format (formatter last-modified-formatter) (java.util.Date.)))


(defn- repo-redirect [{:keys [params]}]
  (log/info params)
  (resp/redirect (str "/" (:repo-url params))))

(defn png-status-resp [filepath]
  (log/info "serving status image" filepath)
  (-> filepath
    (resp/resource-response)
    (resp/header "cache-control" "no-cache")
    (resp/header "last-modified" (last-modified))
    (resp/header "content-type" "image/png")))

(defn svg-status-resp [filepath]
  (log/info "serving status image" filepath)
  (-> filepath
    (resp/resource-response)
    (resp/header "cache-control" "no-cache")
    (resp/header "last-modified" (last-modified))
    (resp/header "content-type" "image/svg+xml")))


(defn get-ref [type repo-owner repo-name]
  (let [owner (clojure.string/replace repo-owner #"\." "-")
        name (clojure.string/replace repo-name #"\." "-")]
      (m/get-in fire-root [type owner name])))

(defroutes app-routes
  (GET "/" [] (index-view/index))

  (HEAD "/" [] "")

  (POST "/find" [] repo-redirect)

  (GET "/:repo-owner/:repo-name" [repo-owner repo-name]
    (try
      (do
        (log/info "processing" repo-owner repo-name)
        (m/swap!
          (get-ref :repos repo-owner repo-name)
          (fn [c] (if (nil? c) 1 (inc c))))
        (if-let [project (statuses/project-map repo-owner repo-name)]
            (do
              (log/info "project-def" project)
              (project-view/index project))
            (do
              (rollbar/report-message (env :rollbar-token)
                "production"
                (str "Repo " repo-owner "/" repo-name "not found") "error")
              (resp/redirect "/"))))
      (catch Exception e
        (do
          (rollbar/report-exception (env :rollbar-token) "production" e)
          (log/error "error happened during processing" e)
          (resp/redirect "/")))))

  (GET "/:repo-owner/:repo-name/status.png" [repo-owner repo-name]
    (try
      (do
        (m/swap!
          (get-ref :statuses repo-owner repo-name)
          (fn [c] (if (nil? c) 1 (inc c))))
       (let [project (statuses/project-map repo-owner repo-name)
             out-of-date-count (:out-of-date (:stats project))]
             (if (> out-of-date-count 0)
               (png-status-resp "public/images/out-of-date.png")
               (png-status-resp "public/images/up-to-date.png"))))
      (catch Exception e
        (rollbar/report-exception (env :rollbar-token) "production" e)
        {:status 404})))

  (GET "/:repo-owner/:repo-name/status.svg" [repo-owner repo-name]
    (try
      (do
        (m/swap!
          (get-ref :statuses repo-owner repo-name)
          (fn [c] (if (nil? c) 1 (inc c))))
       (let [project (statuses/project-map repo-owner repo-name)
             out-of-date-count (:out-of-date (:stats project))]
             (if (> out-of-date-count 0)
               (svg-status-resp "public/images/out-of-date.svg")
               (svg-status-resp "public/images/up-to-date.svg"))))
      (catch Exception e
        (rollbar/report-exception (env :rollbar-token) "production" e)
        {:status 404})))

  (GET "/:repo-owner/:repo-name/downloads.svg" [repo-owner repo-name]
    (try
      (do
        (m/swap!
          (get-ref :downloads repo-owner repo-name)
          (fn [c] (if (nil? c) 1 (inc c))))
        (-> (downloads/get-badge repo-owner repo-name)
            (resp/response)
            (resp/header "cache-control" "no-cache")
            (resp/header "last-modified" (last-modified))
            (resp/header "content-type" "image/svg+xml")))
      (catch Exception e
        (rollbar/report-exception (env :rollbar-token) "production" e)
        {:status 404})))

  (GET "/:repo-owner/:repo-name/status.json" [repo-owner repo-name]
    (try
      (do
        (m/swap!
          (get-ref :statuses repo-owner repo-name)
          (fn [c] (if (nil? c) 1 (inc c))))
        (let [project (statuses/project-map repo-owner repo-name)]
             (project-json/render project)))
      (catch Exception e
        (rollbar/report-exception (env :rollbar-token) "production" e)
        {:status 404})))

  (GET "/:any" []
       (resp/redirect "/")))

(def app
  (-> #'app-routes
     (wrap-json-response)
     (wrap-resource "public")
     (wrap-base-url)
     (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))

(defn -main [& args]
  (let [ip "0.0.0.0"
        port (Integer/parseInt (get (System/getenv) "PORT" "8090"))]
      (run-jetty app {:host ip :port port})))
