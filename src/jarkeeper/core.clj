(ns jarkeeper.core
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE HEAD]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.json :refer [wrap-json-response]]
            [hiccup.middleware :refer [wrap-base-url]]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.handler :as handler]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [jarkeeper.views.index :as index-view]
            [jarkeeper.views.project :as project-view]
            [jarkeeper.views.json :as project-json]
            [ancient-clj.core :as anc])

  (:import (java.io PushbackReader)
           [java.text SimpleDateFormat]
           [java.util Locale TimeZone]))

(def last-modified-formatter "EEE, dd MMM yyyy HH:mm:ss zzz")

(defn- ^SimpleDateFormat formatter [format]
  (doto (SimpleDateFormat. ^String format Locale/US)
    (.setTimeZone (TimeZone/getTimeZone "GMT"))))

(defn last-modified []
  (.format (formatter last-modified-formatter) (java.util.Date.)))

(defn- starting-num? [string]
  (some-> string
          name
          first
          str
          read-string
          number?))

(defn safe-read [s]
  (binding [*read-eval* false]
    (read s)))

(defn read-file
  "Reads all forms in a file lazily."
  [r]
  (binding [*read-eval* false]
    (let [x (read r false ::eof)]
      (if-not (= ::eof x)
        (cons x (lazy-seq (read-file r)))))))

(defn read-project-clj [repo-owner repo-name]
  (try
    (let [url (str "https://raw.github.com/" repo-owner "/" repo-name "/master/project.clj")]
      (with-open [rdr (PushbackReader. (io/reader url))]
        (safe-read rdr)))
    (catch Exception e
      nil)))

(defn read-boot-deps
  "Tries to read first set-env! form in the build.boot file and use that for dependencies."
  [parsed-build-file]
  (some->> parsed-build-file
           (some (fn [form]
                   (if (= 'set-env! (first form))
                     form)))
           rest
           (apply hash-map)
           :dependencies
           last))

(defn read-build-boot [repo-owner repo-name]
  (try
    (let [url (str "https://raw.github.com/" repo-owner "/" repo-name "/master/build.boot")]
      (with-open [rdr (PushbackReader. (io/reader url))]
        (read-boot-deps (read-file rdr))))
    (catch Exception e
      nil)))

(defn check-deps [deps]
  (map #(conj % (anc/artifact-outdated? % {:snapshots? false :qualified? false})) deps))

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
                 (if-let [dependencies (concat (:dependencies profile) (:plugins profile))]
                   (if-let [deps (check-deps dependencies)]
                     [profile-name deps (calculate-stats deps)])))))
       profiles))

(defn boot-project-map [repo-owner repo-name]
  (let [github-url (str "https://github.com/" repo-owner "/" repo-name)]
       (if-let [dependencies (read-build-boot repo-owner repo-name)]
           (do
             (println "boot-build deps" read-boot-deps)
             (let [deps (check-deps dependencies)
                   stats (calculate-stats deps)
                   result { :boot? true
                            :name repo-name
                            :repo-name repo-name
                            :repo-owner repo-owner
                            :github-url github-url
                            :deps deps
                            :stats stats
                            }]
               (log/info "boot project map" result)
               result)))))

(defn lein-project-map [repo-owner repo-name]
  (let [github-url (str "https://github.com/" repo-owner "/" repo-name)]
        (if-let [project-clj-content (read-project-clj repo-owner repo-name)]
            (do
              (println "project-clj" project-clj-content)
              (let [[_ project-name version & info] project-clj-content
                    info-map (apply hash-map info)
                    deps (check-deps (:dependencies info-map))
                    plugins (check-deps (:plugins info-map))
                    profiles (check-profiles (:profiles info-map))
                    stats (calculate-stats deps)
                    plugins-stats (calculate-stats plugins)
                    result (assoc info-map
                             :lein? true
                             :name project-name
                             :repo-name repo-name
                             :repo-owner repo-owner
                             :version version
                             :github-url github-url
                             :deps deps
                             :profiles profiles
                             :plugins plugins
                             :stats stats
                             :plugins-stats plugins-stats)]
                (log/info "project map" result profiles)
                result)))))

(defn project-map [repo-owner repo-name]
  (let [lein-result (future (lein-project-map repo-owner repo-name))
        boot-result (future (boot-project-map repo-owner repo-name))]
    (if (nil? @lein-result)
      @boot-result
      @lein-result)))

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

(defroutes app-routes
  (GET "/" [] (index-view/index))

  (HEAD "/" [] "")

  (POST "/find" [] repo-redirect)

  (GET "/:repo-owner/:repo-name" [repo-owner repo-name]
    (try
      (log/info "processing" repo-owner repo-name)
      (let [project (project-map repo-owner repo-name)]
           (log/info "project-def" project)
           (project-view/index project))
      (catch Exception e
        (log/error "error happened during processing" e)
        (resp/redirect "/"))))

  (GET "/:repo-owner/:repo-name/status.png" [repo-owner repo-name]
    (try
       (let [project (project-map repo-owner repo-name)
             out-of-date-count (:out-of-date (:stats project))]
             (if (> out-of-date-count 0)
               (png-status-resp "public/images/out-of-date.png")
               (png-status-resp "public/images/up-to-date.png")))
      (catch Exception e {:status 404})))

  (GET "/:repo-owner/:repo-name/status.svg" [repo-owner repo-name]
    (try
       (let [project (project-map repo-owner repo-name)
             out-of-date-count (:out-of-date (:stats project))]
             (if (> out-of-date-count 0)
               (svg-status-resp "public/images/out-of-date.svg")
               (svg-status-resp "public/images/up-to-date.svg")))
      (catch Exception e {:status 404})))

  (GET "/:repo-owner/:repo-name/status.json" [repo-owner repo-name]
    (try
      (let [project (project-map repo-owner repo-name)]
           (project-json/render project))
      (catch Exception e {:status 404})))

  (GET "/:any" []
       (resp/redirect "/")))

(def app
  (-> #'app-routes
     (wrap-json-response)
     (wrap-resource "public")
     (wrap-base-url)
     (handler/site)))

(defn -main [& args]
  (let [ip (get (System/getenv) "OPENSHIFT_CLOJURE_HTTP_IP" "0.0.0.0")
        port (Integer/parseInt (get (System/getenv) "OPENSHIFT_CLOJURE_HTTP_PORT" "8090"))]
      (run-jetty app {:host ip :port port})))
