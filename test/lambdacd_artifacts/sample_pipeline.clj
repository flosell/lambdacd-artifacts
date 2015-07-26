(ns lambdacd-artifacts.sample-pipeline
  (:use [compojure.core]
        [lambdacd.steps.control-flow])
  (:require [lambdacd.steps.shell :as shell]
            [lambdacd.steps.manualtrigger :as manualtrigger]
            [lambdacd.core :as lambdacd]
            [ring.server.standalone :as ring-server]
            [lambdacd.util :as util]
            [lambdacd.ui.ui-server :as ui]
            [lambdacd.steps.support :as step-support]
            [lambdacd.runners :as runners]
            [lambdacd-artifacts.core :as artifacts]
            [ring.util.response :as resp]))

(defn some-failing-step [args ctx]
  (shell/bash ctx "/"
              "exit 1"))

(defn produce-output [_ ctx cwd]
  (shell/bash ctx cwd "echo yay > foo.txt"))

(defn some-successful-step [args ctx]
  (let [cwd (util/create-temp-dir)]
    (util/with-temp cwd
      (step-support/chain args ctx
        (produce-output cwd)
        (artifacts/publish-artifacts cwd ["foo.txt"])))))

(defn wait-for-interaction [args ctx]
  (manualtrigger/wait-for-manual-trigger nil ctx))

(def pipeline-structure `(
 (run wait-for-interaction)
  (either
    some-failing-step
    some-successful-step)))


(def artifacts-path-context "/artifacts")

(defn mk-routes [ pipeline-routes artifacts]
  (routes
    (GET "/" [] (resp/redirect "pipeline/"))
    (context "/pipeline" [] pipeline-routes)
    (context artifacts-path-context [] artifacts)))

(defn -main [& args]
  (let [home-dir (if (not (empty? args)) (first args) (util/create-temp-dir))
        config {:home-dir home-dir
                :artifacts-path-context artifacts-path-context}
        pipeline (lambdacd/assemble-pipeline pipeline-structure config)]
    (runners/start-one-run-after-another pipeline)
    (ring-server/serve (mk-routes (ui/ui-for pipeline)
                                  (artifacts/artifact-handler-for pipeline))
                                  {:open-browser? true
                                   :port 8081})))