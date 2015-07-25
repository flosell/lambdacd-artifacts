(ns lambdacd-artifacts.core
  (:require [clojure.java.io :as io]))

(defn publish-artifacts [working-directory patterns args ctx]
  (io/copy (io/file working-directory (first patterns)) (io/file (:home-dir (:config ctx)) (first patterns)))
  {:status :success
   :details []})