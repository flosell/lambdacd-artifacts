(ns lambdacd-artifacts.test-utils-test
  (:require [clojure.test :refer :all]
            [lambdacd-artifacts.test-utils :refer :all]
            [clojure.java.io :as io]
            [lambdacd-artifacts.test-utils :as test-utils]))

(defn- can-read-and-write [f]
  (spit f "foo")
  (is (= "foo" (slurp f))))

(deftest file-with-parents-test
  (testing "that it references a file in the parent folder"
    (can-read-and-write (file-with-parents (test-utils/create-temp-dir) "foo")))
  (testing "that it references a file in a child folder that existed before"
    (let [dir (test-utils/create-temp-dir)]
      (io/make-parents dir "foo" "bar")
      (can-read-and-write (file-with-parents dir "foo" "bar"))))
  (testing "that it references a file in a child folder that didnt exist before"
    (can-read-and-write (file-with-parents (test-utils/create-temp-dir) "foo" "bar" "baz"))))
