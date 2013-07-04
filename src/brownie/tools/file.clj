(ns brownie.tools.file
  (:require [me.raynes.fs :as fs]))

(defn find-by-regexp
  "Finds all files name of which mathes given regexp (at least one from the
  list) in directory subtree that starts at given root. Returns paths to found
  files."
  [root & regexps]
  ;; TODO work with relative paths (for instance, "~/some_dir")
  (letfn [(needed-file? [s] (some #(re-seq % s) regexps))
          (find-files
            [root dirs files]
            (map (partial str root "/") (filter needed-file? files)))]
    ;; TODO check whether apply concat lazy
    (apply concat (fs/walk find-files root))))

(defn find-by-extension
  "Finds all files with given extensions (specified without the dot) in
  directory subtree that starts at given root. Returns paths to found files."
  [root & exts]
  (apply find-by-regexp root (map #(re-pattern (str ".+" "\\." %)) exts)))

(defn paths->files
  "Creates a list of java.io.File instances from given list of file paths."
  [paths]
  (map fs/file paths))

(defn total-size
  "Calculates total size of given list of files."
  [files]
  {:pre [(every? (partial instance? java.io.File) files)]}
  (apply + (map #(.length %) files)))

(defn copy-files
  "Copies all given files to given destination directory."
  [dst files]
  {:pre [(every? (partial instance? java.io.File) files)
         (or (instance? java.io.File dst) (string? dst))]}
  (let [dst (fs/file dst)]
    (doseq [file files]
      (fs/copy file (fs/file dst (.getName file))))))
