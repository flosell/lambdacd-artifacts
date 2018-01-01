(ns lambdacd-artifacts.test-utils
  (:require [clojure.java.io :as io])

  (:import (java.nio.file.attribute FileAttribute)
           (java.nio.file Files)))

(defn- no-file-attributes []
  (into-array FileAttribute []))

(def temp-prefix "lambdacd-artifacts-test")

(defn create-temp-dir []
  (str (Files/createTempDirectory temp-prefix (no-file-attributes))))

(defn file-with-parents [parent & child-path-parts]
  (apply io/make-parents parent child-path-parts)
  (apply io/file parent child-path-parts))
