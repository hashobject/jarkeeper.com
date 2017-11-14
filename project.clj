(defproject jarkeeper "0.6.0-SNAPSHOT"
  :description "Identify outdated dependencies in your Clojure project."
  :url "http://jarkeeper.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.6.0"]
                 [hiccup "2.0.0-alpha1"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-core "1.6.3"]
                 [ring-server "0.5.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ancient-clj "0.3.11"]
                 [ring/ring-json "0.4.0"]
                 [slingshot/slingshot "0.12.2"]
                 [environ "1.0.1"]
                 [clj-rollbar "0.0.3"]
                 [matchbox "0.0.8-SNAPSHOT"]
                 [clj-http "2.0.0"]]
  :main jarkeeper.core
  :ring {:handler jarkeeper.core/app }
  :plugins [[lein-ring "0.12.1"]
            [jonase/eastwood "0.2.1"]
            ;[lein-kibit "0.1.2"]
            ]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"
                "maven-central" "https://central.maven.org/maven2/"},
  :profiles {
    :dev {
      :dependencies [[ring-mock "0.1.5"]
                     [ring/ring-devel "1.4.0"]]}
    :prod {
      :ring {:open-browser? false, :stacktraces? false, :auto-reload? false}}})
