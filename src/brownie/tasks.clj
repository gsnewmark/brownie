(ns brownie.tasks
  (:require [brownie.tools.file :as f]))

(defn gather-files-for-size
  "Copies a set of random files from the given root directory (and its
  subdirectories) with given extensions to the given destination
  directory. The total size of gathered files is smaller or equal to the given
  maximum size (specified in megabytes)."
  [dst max-size root & extensions]
  {:pre [(number? max-size) (or (string? root) (instance? java.io.File root))
         (every? string? extensions)]}
  (let [max-size (* max-size 1024 1024)]
    (->> (apply f/find-by-extension root extensions)
         f/paths->files
         shuffle
         (reductions (fn [s f] (conj s f)) #{})
         (take-while (fn [s] (>= max-size (f/total-size s))))
         last
         (f/copy-files dst))))
