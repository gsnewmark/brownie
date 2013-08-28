(ns brownie.tasks
  (:require [brownie.tools.file :as f]))

(defn gather-random-files
  "Copies a set of random files from the given root directory (and its
  subdirectories) with given extensions to the given destination
  directory. The total size of gathered files is smaller or equal to the given
  maximum size (specified in megabytes). Destination directory is cleaned
  before copying new files."
  [root dst max-size & extensions]
  {:pre [(or (string? root) (instance? java.io.File root))
         (every? string? extensions)]}
  (f/clean-dir dst)
  (let [max-size (* (Double. max-size) 1024 1024)]
    (->> (apply f/find-by-extension root extensions)
         f/paths->files
         shuffle
         (reductions (fn [s f] (conj s f)) #{})
         (take-while (fn [s] (>= max-size (f/total-size s))))
         last
         (f/copy-files dst))))
