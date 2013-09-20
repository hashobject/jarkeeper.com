(defproject jarkeeper.com "0.1.0-SNAPSHOT"
  :description "Identify out of date dependecies in your Clojure project."
  :url "http://jarkeeper.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [ring/ring-core "1.2.0"]
                 [ring-server "0.3.0"]
                 [org.clojure/tools.logging "0.2.6"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [ancient-clj "0.1.3"]]
  :ring {:handler jarkeeper.core/app }
  :plugins [[lein-ring "0.8.6"]]
  :profiles {
    :dev {
      :dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.2.0"]]}
    :prod {
      :ring {:open-browser? false, :stacktraces? false, :auto-reload? false}}})
