(ns jarkeeper.checker
(:require   [leiningen.ancient.projects :refer [dependency-map repository-maps collect-dependencies]]
            [leiningen.ancient.version :refer [version-map]]))

(def test-project
  '{:repositories [["str" "http://string/repo"]
                   ["map" {:url "http://map/repo"}]]
    :dependencies [[group/artifact "1.0.0"]
                   [org.clojure/clojure "1.5.1"]]
    :plugins [[group/plugin "0.1.0"]]
    :profiles {:xyz {:dependencies [[xyz "1.2.3"]]
                     :plugins [[xyz-plugin "3.2.1"]]}}})




