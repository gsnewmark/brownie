(ns brownie.file
  (:require [me.raynes.fs :as fs]))

(defn find-by-regexp
  "Finds all files name of which mathes given regexp (at least one from the
  list) in directory subtree that starts at given root."
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
  directory subtree that starts at given root."
  [root & exts]
  (apply find-by-regexp root (map #(re-pattern (str ".+" "\\." %)) exts)))

;;; TODO should be random (i. e., collect different files on each run)
(defn collect-files
  [max-size filepaths]
  (let [files (shuffle (map fs/file filepaths))
        file-size-tuples (shuffle (map (fn [f] [f (.length f)]) files))
        tuples->map (fn [m [f s]]
                      (let [files (conj (get m s #{}) f)] (assoc m s files)))
        size-files-map (reduce tuples->map (sorted-map) file-size-tuples)
        collect (fn [{:keys [total-size] :as m} [size files]]
                  (if (> size 0)
                    (let [num-of-files-to-take
                          (min (count files)
                               (Math/floor (/ (- max-size total-size) size)))

                          files-size
                          (* size num-of-files-to-take)]
                      (-> m
                          (update-in [:files] into
                                     (take num-of-files-to-take files))
                          (update-in [:total-size] + files-size)))
                    m))]
    (reduce collect {:files #{} :total-size 0} size-files-map)))
