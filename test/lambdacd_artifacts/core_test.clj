(ns lambdacd-artifacts.core-test
  (:require [clojure.test :refer :all]
            [lambdacd-artifacts.core :refer :all]))

(deftest dummy-test
  (testing "that the test runs"
    (is (= {:status :success} (dummy nil nil)))))
