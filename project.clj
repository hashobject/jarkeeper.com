(defproject jarkeeper "0.6.0-SNAPSHOT"
  :description "Identify outdated dependencies in your Clojure project."
  :url "http://jarkeeper.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [compojure "1.6.1"]
                 [hiccup "2.0.0-alpha1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-core "1.7.1"]
                 [ring-server "0.5.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ancient-clj "0.6.15"]
                 [ring/ring-json "0.4.0"]
                 [slingshot/slingshot "0.12.2"]
                 [environ "1.1.0"]
                 [clj-rollbar "0.0.3"]
                 [matchbox "0.0.9"]
                 [clj-http "3.9.1"]]
  :main jarkeeper.core
  :ring {:handler jarkeeper.core/app }
  :plugins [[lein-ring "0.12.4"]
            [jonase/eastwood "0.2.5"]
            ]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"
                "maven-central" "https://central.maven.org/maven2/"},
  :profiles {
    :dev {
      :ring {:open-browser? false}
      :dependencies [[ring-mock "0.1.5"]
                     [ring/ring-devel "1.7.1"]]}
    :prod {
      :ring {:open-browser? false, :stacktraces? false, :auto-reload? false}}})
