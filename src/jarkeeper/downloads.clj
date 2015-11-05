(ns jarkeeper.downloads
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.client :as client]))


(def downloads-svg-file
  (-> "public/images/downloads.svg"
      io/resource
      io/file))

(def downloads-svg (slurp downloads-svg-file))

(def no-downloads-svg-file
  (-> "public/images/no-downloads.svg"
      io/resource
      io/file))

(def no-downloads-svg (slurp no-downloads-svg-file))

(defn create-badge [downloads]
  (str/replace downloads-svg #"PLACEHOLDER" downloads))


(defn clojars-fetch [artifact]
  (try
    (client/get (str "https://clojars.org/api/artifacts/" artifact) {:accept :json :as :json})
    (catch Exception e
      {:data e})))


(defn get-downloads [repo-owner repo-name]
  (let [resp-by-short-name (clojars-fetch repo-name)
        resp-by-full-name (clojars-fetch (str repo-owner "/" repo-name))]
    (if (= 200 (:status resp-by-short-name))
      (some-> resp-by-short-name :body :downloads)
      (some-> resp-by-full-name :body :downloads))))

(defn format-downloads [downloads]
  (let [s (str downloads)
        digits (count s)]
        (if (< digits 5)
          s
          (str
            (apply str (drop-last 3 s))
            "K"))))

(defn get-badge [repo-owner repo-name]
  (if-let [downloads (get-downloads repo-owner repo-name)]
    (-> downloads
        format-downloads
        create-badge)
    ; if no downloads found
    no-downloads-svg))
