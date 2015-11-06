(ns jarkeeper.statuses-test
  (:require [clojure.test :refer :all]
            [jarkeeper.statuses :refer :all]
            [clojure.java.io :as io])
  (:import [java.io PushbackReader]))

(def project-clj-basic "
(defproject jarkeeper \"0.5.1-SNAPSHOT\"
  :dependencies [[org.clojure/clojure \"1.6.0\"]])")

(def project-clj-with-extra-defs "
(def driver-version \"2.1.8\")
(defproject jarkeeper \"0.5.1-SNAPSHOT\"
  :dependencies [[org.clojure/clojure \"1.6.0\"]])")

(def project-clj-eval "
(defproject jarkeeper \"0.5.1-SNAPSHOT\"
  :dependencies [[org.clojure/clojure \"1.6.0\"]]
  :foo #(throw \"MUST NOT BE EVALUATED\"))")

(deftest read-project-clj-test
  (is (= '(defproject jarkeeper "0.5.1-SNAPSHOT" :dependencies [[org.clojure/clojure "1.6.0"]])
         (with-open [rdr (PushbackReader. (io/reader (.getBytes project-clj-basic)))]
           (read-lein-project (read-file rdr)))))

  (is (= '(defproject jarkeeper "0.5.1-SNAPSHOT" :dependencies [[org.clojure/clojure "1.6.0"]])
        (with-open [rdr (PushbackReader. (io/reader (.getBytes project-clj-with-extra-defs)))]
          (read-lein-project (read-file rdr)))))

  (is (= '(defproject jarkeeper "0.5.1-SNAPSHOT"
            :dependencies [[org.clojure/clojure "1.6.0"]]
            :foo (fn* [] (throw "MUST NOT BE EVALUATED")))
         (with-open [rdr (PushbackReader. (io/reader (.getBytes project-clj-eval)))]
           (read-lein-project (read-file rdr))))))

(def build-boot "
  (set-env! :dependencies [[adzerk-oss/boot-cljs \"1.7.48-6\"]])")

(def build-boot-env-not-first "
  (println \"foo\")
  (set-env! :dependencies [[adzerk-oss/boot-cljs \"1.7.48-6\"]])")

(deftest read-boot-deps-test
  (is (= '((set-env! :dependencies [[adzerk-oss/boot-cljs "1.7.48-6"]]))
         (with-open [rdr (PushbackReader. (io/reader (.getBytes build-boot)))]
           (doall (read-file rdr)))))

  (is (= '((println "foo") (set-env! :dependencies [[adzerk-oss/boot-cljs "1.7.48-6"]]))
         (with-open [rdr (PushbackReader. (io/reader (.getBytes build-boot-env-not-first)))]
           (doall (read-file rdr))))) )

(deftest read-boot-deps-test
  (is (= '[adzerk-oss/boot-cljs "1.7.48-6"]
         (read-boot-deps '((println "foo") (set-env! :dependencies [[adzerk-oss/boot-cljs "1.7.48-6"]])))))

  (testing "read-build-deps can be used when reading"
    (is (= '[adzerk-oss/boot-cljs "1.7.48-6"]
           (with-open [rdr (PushbackReader. (io/reader (.getBytes build-boot-env-not-first)))]
             (read-boot-deps (read-file rdr)))))) )
