(ns jarkeeper.views.json
  (:require [ring.util.response :refer [response]]))

(defn transform-dependency [[dep-name version _ exclusions latest]]
  (let [up-to-date     (nil? latest)
        out-of-date    (not up-to-date)
        latest-version (if up-to-date version (latest :version-string))]
    {:name           (str dep-name)
     :version        version
     :is-up-to-date  up-to-date
     :is-out-of-date out-of-date
     :latest-version latest-version}))

(defn transform-stats [{:keys [total up-to-date out-of-date]}]
  (let [is-up-to-date  (= total up-to-date)
        is-out-of-date (not is-up-to-date)]
    {:total         total
    :up-to-date     up-to-date
    :out-of-date    out-of-date
    :is-up-to-date  is-up-to-date
    :is-out-of-date is-out-of-date}))

(defn transform-dependencies [{:keys [deps stats]}]
  {:dependencies (map transform-dependency deps)
   :stats        (transform-stats stats)})

(defn transform-plugins [{:keys [plugins plugins-stats]}]
  {:plugins (map transform-dependency plugins)
   :stats   (transform-stats plugins-stats)})

(defn transform-profile [[profile-name dependencies stats]]
  {profile-name {:dependencies (map transform-dependency dependencies)
                 :stats        (transform-stats stats)}})

(defn transform-profiles [{:keys [profiles]}]
  {:profiles (apply merge (map transform-profile profiles))
  :stats    (transform-stats (apply merge-with + (map #(nth % 2) profiles)))})

(defn stats [dependencies plugins profiles]
  (let [everything     [dependencies plugins profiles]
        total          (apply + (map #(get-in % [:stats :total])       everything))
        up-to-date     (apply + (map #(get-in % [:stats :up-to-date])  everything))
        out-of-date    (apply + (map #(get-in % [:stats :out-of-date]) everything))
        is-up-to-date  (= total up-to-date)
        is-out-of-date (not is-up-to-date)]
    {:total         total
    :up-to-date     up-to-date
    :out-of-date    out-of-date
    :is-up-to-date  is-up-to-date
    :is-out-of-date is-out-of-date}))

(defn transform [project]
  (let [dependencies (transform-dependencies project)
        plugins      (transform-plugins      project)
        profiles     (transform-profiles     project)]
    {:name         (project :name)
     :version      (project :version)
     :description  (project :description)
     :github       {:owner (project :repo-owner)
                    :name  (project :repo-name)
                    :url   (project :github-url)}
     :dependencies dependencies
     :plugins      plugins
     :profiles     profiles
     :stats        (stats dependencies plugins profiles)}))

(defn render [project]
  (response (transform project)))

