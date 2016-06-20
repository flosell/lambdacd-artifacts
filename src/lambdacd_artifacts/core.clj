(ns lambdacd-artifacts.core
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [ring.util.response :as response]
            [compojure.route :refer :all]
            [compojure.core :refer :all])
  (:import (java.io File)
           (java.nio.file Paths)))

(defn- find-latest-artifact [home-dir step-id path]
  (let [home-file (io/file home-dir)
        build-directories (.listFiles home-file)
        latest-build-directory (last (filter #(.exists (io/file % step-id path)) build-directories))]
    (io/file latest-build-directory step-id)))

(defn root-path [home-dir build-number step-id path]
  (if (= build-number "latest")
         (find-latest-artifact home-dir step-id path)
         (io/file home-dir (str build-number) step-id)))

(defn file-result [home-dir build-number step-id path]
  (let [root (root-path home-dir build-number step-id path)
        file-response (response/file-response path {:root (str root)})]
    (if file-response
      file-response
      (response/not-found (str "could not find " path " for build number " build-number " and step-id " step-id)))))

(defn artifact-handler-for [pipeline]
  (let [home-dir (:home-dir (:config (:context pipeline)))]
    (routes
      (GET "/:buildnumber/:stepid/*" [buildnumber stepid *]
        (file-result home-dir buildnumber stepid *)))))

(defn relative-path [base-dir file]
  (let [base-path (Paths/get (.toURI base-dir))
        file-path (Paths/get (.toURI file))]
    (.relativize base-path file-path)))

(defn- file-matches [base-dir regex-or-string]
  (if (string? regex-or-string)
    (fn [file]
      (= regex-or-string (str (relative-path base-dir file))))
    (fn [file]
      (re-matches regex-or-string (str (relative-path base-dir file))))))

(defn- find-files-matching [regex-or-string dir]
  (filter (file-matches dir regex-or-string)
          (file-seq dir)))

(defn format-step-id [step-id]
  (s/join "-" step-id))

(defn- copy-file [{step-id :step-id build-number :build-number { home-dir :home-dir} :config} working-directory input-file]
  (let [file-name      (.getName input-file)
        output-parent  (butlast (s/split (str (relative-path (io/file working-directory) input-file)) #"/"))
        output-parts   (concat [(str build-number) (format-step-id step-id)] output-parent [file-name])
        output-file    (apply io/file home-dir output-parts)]
    (io/make-parents output-file)
    (io/copy input-file output-file)
    output-file))

(defn file-details [{{home-dir :home-dir artifacts-dir :artifacts-path-context} :config} output-file]
    {:label (.getName output-file)
     :href  (str artifacts-dir "/" (relative-path (io/file home-dir) output-file))})

(defn publish-artifacts [args ctx cwd patterns]
  (let [working-dir  (io/file cwd)
        output-files (doall (->> patterns
                                 (map #(find-files-matching % working-dir ))
                                 (flatten)
                                 (filter #(not (.isDirectory %)))
                                 (map #(copy-file ctx working-dir %1))
                                 (flatten)))
        file-details (map #(file-details ctx %) output-files)]
    {:status :success
     :details [{:label "Artifacts"
                :details file-details}]}))