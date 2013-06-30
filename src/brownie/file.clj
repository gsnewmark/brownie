(ns brownie.file
  (:require [me.raynes.fs :as fs]))

(defn find-by-regexp
  "Finds all files name of which mathes given regexp (at least one from the
  list) in directory subtree that starts at given root."
  [root f & regexps]
  ;; TODO work with relative paths (for instance, "~/some_dir")
  (letfn [(needed-file? [s] (some #(re-seq % s) regexps))
          (find-files
            [root dirs files]
            (map (partial str root "/") (filter needed-file? files)))]
    ;; TODO check whether apply concat lazy
    (apply concat (fs/walk find-files root))))

(defn find-by-extension
  "Finds all files with given extensions (specified without the dot) in
  directory subtree that starts at given root."
  [root & exts]
  (apply find-by-regexp root (map #(re-pattern (str ".+" "\\." %)) exts)))
