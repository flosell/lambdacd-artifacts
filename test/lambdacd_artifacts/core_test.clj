(ns lambdacd-artifacts.core-test
  (:require [clojure.test :refer :all]
            [lambdacd-artifacts.core :refer :all]
            [clojure.java.io :as io]
            [lambdacd.util :as util]))

(defn map-containing [expected m]
  (and (every? (set (keys m)) (keys expected))
       (every? #(= (m %)(expected %)) (keys expected))))

(defn- file-in [dir]
  (->> (file-seq (io/file dir))
       (filter #(.isFile %))))

(deftest publish-artifacts-test
  (testing "that a specified file is copied to the archive folder"
    (let [cwd      (util/create-temp-dir)
          home-dir (util/create-temp-dir)]
      (spit (io/file cwd "foo.txt") "hello content")
      (let [publish-artifacts-result (publish-artifacts cwd ["foo.txt"] {} {:config {:home-dir home-dir}})]
        (is (map-containing {:status :success} publish-artifacts-result))
        (is (= "hello content" (slurp (first (file-in home-dir)))))))))

;; TODO: artifact should be in some defined folder in the home-dir
;; TODO: more than one pattern should be supported
;; TODO: actual patterns should be supported
;; TODO: ring handler to read file again needs to be implemented, add an integration test here that first publishes an artifact,
;;       then reads it from the ring handler