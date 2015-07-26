(ns lambdacd-artifacts.core-test
  (:require [clojure.test :refer :all]
            [lambdacd-artifacts.core :refer :all]
            [lambdacd-artifacts.test-utils :refer [file-with-parents]]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]
            [lambdacd.util :as util]))

(defn map-containing [expected m]
  (and (every? (set (keys m)) (keys expected))
       (every? #(= (m %)(expected %)) (keys expected))))

(defn- file-in [dir]
  (->> (file-seq (io/file dir))
       (filter #(.isFile %))))

(defn- ctx [home-dir build-number step-id]
  {:config {:home-dir home-dir}
   :build-number build-number
   :step-id step-id})

(defn- pipeline-with-homedir [home-dir]
  {:context (ctx home-dir 1 "2-3")})

(defn file-path-for [cwd build-number step-id & file-path]
  (apply file-with-parents cwd (str build-number) step-id file-path))

(deftest publish-artifacts-test
  (testing "that a specified file is copied to the archive folder"
    (let [cwd      (util/create-temp-dir)
          home-dir (util/create-temp-dir)
          ctx      (ctx home-dir 1 "2-3")]
      (spit (file-with-parents cwd "foo.txt") "hello content")
      (let [publish-artifacts-result (publish-artifacts {} ctx cwd ["foo.txt"] )]
        (is (map-containing {:status :success} publish-artifacts-result))
        (is (= "hello content" (slurp (file-path-for home-dir 1 "2-3" "foo.txt"))))))))

;; TODO: more than one pattern should be supported
;; TODO: actual patterns should be supported

(deftest artifact-handler-for-test
  (let [home-dir (util/create-temp-dir)
        pipeline (pipeline-with-homedir home-dir)
        handler  (artifact-handler-for pipeline)]
    (spit (file-path-for home-dir 1 "2-3" "some-file") "hello world")
    (spit (file-path-for home-dir 1 "2-3" "some-sub-folder" "some-file") "hello world from subfolder")

    (testing "that it returns existing artifacts correctly"
      (let [response (handler (mock/request :get "/1/2-3/some-file"))]
        (is (= 200 (:status response)))
        (is (= (file-path-for home-dir 1 "2-3" "some-file") (:body response)))))
    (testing "that it returns existing artifacts in subfolders correctly"
      (let [response (handler (mock/request :get "/1/2-3/some-sub-folder/some-file"))]
        (is (= 200 (:status response)))
        (is (= (file-path-for home-dir 1 "2-3" "some-sub-folder" "some-file") (:body response)))))
    (testing "that it returns 404 if artifact doesn't exist"
      (let [response (handler (mock/request :get "/1/2-3/some-file-that-doesnt-exist"))]
        (is (= 404 (:status response)))
        (is (.contains (:body response) "1"))
        (is (.contains (:body response) "2-3"))
        (is (.contains (:body response) "some-file-that-doesnt-exist"))))
    (testing "that it returns 404 if artifact doesn't exist for this build"
      (let [response (handler (mock/request :get "/2/2-3/some-file"))]
        (is (= 404 (:status response)))))
    (testing "that it returns 404 if trying to break out of current working directory"
      (spit (file-path-for home-dir 3 "2-3" "some-file") "hello world") ; create a base from which we could break out
      (let [response (handler (mock/request :get "/3/2-3/../../1/2-3/some-file"))]
        (is (= 404 (:status response)))))))