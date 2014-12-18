(defproject jarkeeper "0.4.0-SNAPSHOT"
  :description "Identify outdated dependencies in your Clojure project."
  :url "http://jarkeeper.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [hiccup "1.0.5"]
                 [ring/ring-core "1.3.2"]
                 [ring-server "0.3.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [ancient-clj "0.2.1"]
                 [ring/ring-json "0.3.1"]]
  :ring {:handler jarkeeper.core/app }
  :plugins [[lein-ring "0.8.13"]]
  :profiles {
    :dev {
      :dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.2"]]}
    :prod {
      :ring {:open-browser? false, :stacktraces? false, :auto-reload? false}}})
