(def lambdacd-version (or
                        (System/getenv "LAMBDACD_VERSION")
                        "0.13.5"))

(def clojure-version-to-use (or
                              (System/getenv "CLOJURE_VERSION")
                              "1.7.0"))


(defproject lambdacd-artifacts "0.2.2-SNAPSHOT"
  :description "provides a way to access build artifacts generated by a step in LambdaCD"
  :url "http://github.com/flosell/lambdacd-artifacts"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :deploy-repositories [["clojars" {:creds :gpg}]
                        ["releases" :clojars]]
  :dependencies [[org.clojure/clojure ~clojure-version-to-use]
                 [compojure "1.6.0"]
                 [lambdacd ~lambdacd-version]]
  :test2junit-run-ant true
  :profiles {:dev {:dependencies [[ring-server "0.4.0"]
                                  [ring/ring-mock "0.2.0"]]
                   :main lambdacd-artifacts.sample-pipeline
                   :plugins [[test2junit "1.1.1"]]}})
