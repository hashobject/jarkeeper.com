(defproject jarkeeper "0.6.0-SNAPSHOT"
  :description "Identify outdated dependencies in your Clojure project."
  :url "http://jarkeeper.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.0"]
                 [hiccup "1.0.5"]
                 [ring/ring-defaults "0.2.0"]
                 [ring/ring-core "1.4.0"]
                 [ring-server "0.4.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ancient-clj "0.3.14"]
                 [ring/ring-json "0.4.0"]
                 [slingshot/slingshot "0.12.2"]
                 [environ "1.0.2"]
                 [clj-rollbar "0.0.3"]
                 [matchbox "0.0.8"]
                 [clj-http "3.0.1"]]
  :main jarkeeper.core
  :ring {:handler jarkeeper.core/app }
  :plugins [[lein-ring "0.9.7"]
            [jonase/eastwood "0.2.3"]
            [lein-kibit "0.1.2"]]
  :profiles {
    :dev {
      :dependencies [[ring-mock "0.1.5"]
                     [ring/ring-devel "1.4.0"]]}
    :prod {
      :ring {:open-browser? false, :stacktraces? false, :auto-reload? false}}})
