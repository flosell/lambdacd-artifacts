(ns lambdacd-artifacts.core
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [ring.util.response :as response]
            [compojure.route :refer :all]
            [compojure.core :refer :all]))


(defn file-result [home-dir build-number step-id path]
  (let [root (io/file home-dir (str build-number) step-id)
        file-response (response/file-response path {:root (str root)})]
    (if file-response
      file-response
      (response/not-found (str "could not find " path " for build number " build-number " and step-id " step-id)))))

(defn artifact-handler-for [pipeline]
  (let [home-dir (:home-dir (:config (:context pipeline)))]
    (routes
      (GET "/:buildnumber/:stepid/*" [buildnumber stepid *]
        (file-result home-dir buildnumber stepid *)))))

(defn publish-artifacts [args ctx working-directory patterns]
  (let [home-dir     (:home-dir (:config ctx))
        build-number (:build-number ctx)
        step-id      (:step-id ctx)
        output-file  (io/file home-dir (str build-number) step-id (first patterns))]
    (io/make-parents output-file)
    (io/copy (io/file working-directory (first patterns)) output-file)
    {:status :success
     :details []}))