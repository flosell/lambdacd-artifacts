(ns lambdacd-artifacts.test-utils
  (:require [clojure.java.io :as io]))

(defn file-with-parents [parent & child-path-parts]
  (apply io/make-parents parent child-path-parts)
  (apply io/file parent child-path-parts))