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


(defn- ctx-with [home-dir build-number step-id artifacts-context]
  {:config {:home-dir home-dir
            :artifacts-path-context artifacts-context}
   :build-number build-number
   :step-id step-id})

(defn- pipeline-with-homedir [home-dir]
  {:context (ctx-with home-dir 1 "2-3" "some-artifacts-dir")})

(defn- file-path-for [cwd build-number step-id & file-path]
  (apply file-with-parents cwd (str build-number) step-id file-path))

(defn artifacts [publish-artifacts-result]
  (->> publish-artifacts-result
       :details
       (map :details)
       (flatten)
       (map :href)
       (flatten)
       (into #{})))

(deftest publish-artifacts-test
  (let [cwd (util/create-temp-dir)
        home-dir (util/create-temp-dir)
        ctx (ctx-with home-dir 1 [2 3] "")
        ctx-with-artifacts-path (ctx-with home-dir 1 [2 3] "artifacts-path")]
    (spit (file-with-parents cwd "foo.txt") "hello content")
    (spit (file-with-parents cwd "foobar.txt") "hello content")
    (spit (file-with-parents cwd "dir1" "bar.txt") "hello subdirectory")

    (testing "that a specified file is copied to the archive folder"
      (publish-artifacts {} ctx cwd ["foo.txt"])
      (is (= "hello content" (slurp (file-path-for home-dir 1 "2-3" "foo.txt")))))
    (testing "the step is successful"
      (is (map-containing {:status :success} (publish-artifacts {} ctx cwd ["foo.txt"]))))
    (testing "that it returns a map with details about the artifacts it published"
      (is (map-containing {:details [{:label   "Artifacts"
                                      :details [{:label "foo.txt"
                                                 :href  "artifacts-path/1/2-3/foo.txt"}]}]} (publish-artifacts {} ctx-with-artifacts-path cwd ["foo.txt"]))))
    (testing "that it can match exact paths"
      (is (= #{"/1/2-3/foo.txt"}
             (artifacts (publish-artifacts {} ctx cwd ["foo.txt"]))))
      (is (= #{"/1/2-3/dir1/bar.txt"}
             (artifacts (publish-artifacts {} ctx cwd ["dir1/bar.txt"]))))
      (is (= #{"/1/2-3/dir1/bar.txt"
               "/1/2-3/foo.txt"}
             (artifacts (publish-artifacts {} ctx cwd ["dir1/bar.txt" "foo.txt"])))))
    (testing "that it supports regexes"
      (is (= #{"/1/2-3/foo.txt"
               "/1/2-3/foobar.txt"}
             (artifacts (publish-artifacts {} ctx cwd [#"foo.*"]))))
      (is (= #{"/1/2-3/dir1/bar.txt"}
             (artifacts (publish-artifacts {} ctx cwd [#"dir1/.*"]))))
      (is (= #{"/1/2-3/dir1/bar.txt"}
             (artifacts (publish-artifacts {} ctx cwd [#".*/bar.txt"]))))
      (is (= #{"/1/2-3/dir1/bar.txt"
               "/1/2-3/foo.txt"
               "/1/2-3/foobar.txt"}
             (artifacts (publish-artifacts {} ctx cwd [#"(.*)\.txt"])))))))

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

(deftest integration-test
  (testing "that we can publish an artifact and get it back from the details supplied in the step-result"
    (let [cwd      (util/create-temp-dir)
          home-dir (util/create-temp-dir)
          ctx      (ctx-with home-dir 1 [2 3] "")
          pipeline {:context ctx}
          handler  (artifact-handler-for pipeline)]
      (spit (file-with-parents cwd "foo.txt") "hello content")
      (let [publish-artifacts-result (publish-artifacts {} ctx cwd ["foo.txt"])
            first-artifact-href      (:href (first (:details (first (:details publish-artifacts-result)))))
            first-artifact-response  (handler (mock/request :get first-artifact-href))]
        (is (= 200 (:status first-artifact-response)))))))