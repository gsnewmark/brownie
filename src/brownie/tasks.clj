(ns brownie.tasks
  (:require [brownie.tools.file :as f]
            [clojure.math.combinatorics :as combo]))

;;; TODO better algo (not to so slow - generating all subsets is very
;;;      'expensive')
(defn gather-files-for-size
  "Makes a list of random files from given root directory (and its
  subdirectories) with given extensions. The total size of gathered files is
  smaller or equal to the given maximum size (specified in megabytes) but at
  least as big as given percent of maximum size (float in range (0.0, 1.0])."
  [min-size-percent max-size root & extensions]
  {:pre [(number? min-size-percent) (number? max-size)
         (or (string? root) (instance? java.io.File root))
         (every? string? extensions)
         (> min-size-percent 0.0) (<= min-size-percent 1.0)]}
  (let [max-size (* max-size 1024 1024)
        possible-combinations
        (->> (apply f/find-by-extension root extensions)
             f/paths->files
             combo/subsets
             (map (fn [x] [x (f/total-size x)]))
             (filter (fn [[files total-size]]
                       (and (>= max-size total-size)
                            (> (/ total-size max-size) min-size-percent)))))]
    (when-not (empty? possible-combinations)
      (first (rand-nth (shuffle possible-combinations))))))
