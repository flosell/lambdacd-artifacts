(defproject lambdacd-artifacts "0.1.2"
  :description "provides a way to access build artifacts generated by a step in LambdaCD"
  :url "http://github.com/flosell/lambdacd-artifacts"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :deploy-repositories [["clojars" {:creds :gpg}]
                        ["releases" :clojars]]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.1.8"]
                 [lambdacd "0.9.0"]]
  :test2junit-run-ant true
  :profiles {:dev {:dependencies [[ring-server "0.3.1"]
                                  [ring/ring-mock "0.2.0"]]
                   :main lambdacd-artifacts.sample-pipeline
                   :plugins [[test2junit "1.1.1"]]}})
