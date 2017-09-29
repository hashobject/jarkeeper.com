(ns jarkeeper.statuses
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.client :as client]
            [clojure.tools.logging :as log]
            [ancient-clj.core :as anc]
            [clojure.string :as string])

  (:import (java.io PushbackReader)))

(defn- starting-num? [string]
  (some-> string
          name
          first
          str
          read-string
          number?))

(defn sift
  "Takes a predicate and a seq, returns two seqs being respectively the elements for which pred
returned truthy and falsey."
  ;; From https://github.com/jaunt-lang/jaunt/blob/7c7b55633/src/clj/clojure/core.clj#L4597-L4610
  [pred coll]
  (loop [t []
         f []
         [e & coll' :as coll] coll]
    (if (empty? coll)
      [t f]
      (if (pred e)
        (recur (conj t e) f coll')
        (recur t (conj f e) coll')))))

(defn read-file
  "Reads all forms in a file lazily."
  [r]
  (binding [*read-eval* false]
    (let [x (read r false ::eof)]
      (if-not (= ::eof x)
        (cons x (lazy-seq (read-file r)))))))

(defn read-lein-project
  "Tries to read first project form in the project.clj file and use that"
  [parsed-project-file]
  (some->> parsed-project-file
           (some (fn [form]
                   (if (= 'defproject (first form))
                     form)))
))

(defn read-project-clj [repo-owner repo-name]
  (try
    (let [url (str "https://raw.github.com/" repo-owner "/" repo-name "/master/project.clj")]
      (with-open [rdr (PushbackReader. (io/reader url))]
        (read-lein-project (read-file rdr))))
    (catch Exception _
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
    (catch Exception _
      nil)))

(defn check-deps [deps]
  (map #(conj % (anc/artifact-outdated? % {:snapshots? false :qualified? false})) deps))

(defn clojure-dependency? [dep]
  (if (or (= (first dep) "org.clojure/clojure")
          (= (first dep) "org.clojure/clojurescript"))))

(defn calculate-stats [deps]
  (let [filtered-deps (remove clojure-dependency? deps)
        [up-to-date-deps out-of-date-deps] (sift (fn [dep] (nil? (last dep))) filtered-deps)
        stats {:total (count filtered-deps)
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
