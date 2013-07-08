(ns brownie.file-test
  (:require [clojure.test :refer :all]
            [brownie.tools.file :refer :all]
            [me.raynes.fs :as fs]))

(defn create-file
  [root dir name]
  (spit (fs/file root dir name) "tmp"))

(defn create-test-dirs-files []
  (let [root (fs/temp-dir "brownie-")
        music-dir-name "music"
        docs-dir-name "docs"
        create-music-file (partial create-file root music-dir-name)
        create-doc-file (partial create-file root docs-dir-name)]
    (doseq [dir [music-dir-name docs-dir-name]] (fs/mkdir (fs/file root dir)))
    (doseq [file ["1.flac" "2.flac" "3.flac" "a.flac" "b.flac" "c.flac"
                  "1.mp3" "2.mp3" "3.mp3" "a.mp3" "b.mp3" "c.mp3"
                  "1.flac.bak"]]
      (create-music-file file))
    (doseq [file ["1.org" "2.org" "3.org" "a.org" "b.org" "c.org"
                  "1.pdf" "2.pdf" "3.pdf" "a.pdf" "b.pdf" "c.pdf"
                  "1.md" "2.md" "3.md" "a.md" "b.md" "c.md"]]
      (create-doc-file file))
    root))

(defn create-dir-tree-for-delete []
  (let [root (fs/temp-dir "brownie-clean")
        first-dir-name "first"
        first-dir-child-name "first-child"
        second-dir-name "second"]
    (doseq [dir [first-dir-name second-dir-name]]
      (fs/mkdir (fs/file root dir)))
    (fs/mkdir (fs/file root first-dir-name first-dir-child-name))
    (doseq [f ["a" "b" "c"]]
      (create-file (fs/file root first-dir-name) first-dir-child-name f))
    (doseq [f ["a" "b" "c"]] (create-file root second-dir-name f))
    (doseq [f ["a" "b" "c"]] (create-file root first-dir-name f))
    root))

(defn create-dst-dir [] (fs/temp-dir "brownie-dst"))

(def root (create-test-dirs-files))

;;; TODO remove hard-coded numbers

(deftest find-by-regexp-test
  (testing "Find file by part of name."
    (is (= 16 (count (find-by-regexp root #"\d+\..+"))))
    (is (= 16 (count (find-by-regexp root #"[a-zA-Z]\..+"))))
    (is (= 6 (count (find-by-regexp root #"1"))))
    (is (= 6 (count (find-by-regexp root #"b")))))
  (testing "Find file by name and extension."
    (is (= 4 (count (find-by-regexp root #"\d+\.f.+"))))
    (is (= 31 (count (find-by-regexp root #"\w+\..*"))))
    (is (= 12 (count (find-by-regexp root #"\w+\.m"))))
    (is (= 6 (count (find-by-regexp root #"\d+\.m"))))
    (is (= 3 (count (find-by-regexp root #"[a-zA-Z]+\.flac"))))
    (is (= 6 (count (find-by-regexp root #"\w+\.mp3")))))
  (testing "Find file by few regexps."
    (is (= 22 (count (find-by-regexp root #"\w+\.m.*" #"\d\."))))
    (is (= 10 (count (find-by-regexp root #"\.p" #"\.l" #"\d\.f")))))
  (testing "Try to find files that don't exist."
    (is (= 0 (count (find-by-regexp root #"[1-5]*\.k"))))))

(deftest find-by-extension-test
  (testing "Find music files."
    (is (= (+ (count (find-by-extension root "flac"))
              (count (find-by-extension root "mp3")))
           (count (find-by-extension root "flac" "mp3"))))
    (is (= 12 (count (find-by-extension root "flac" "mp3"))))
    (is (= 6 (count (find-by-extension root "flac"))))
    (is (= 6 (count (find-by-extension root "mp3")))))
  (testing "Find doc files."
    (is (= (+ (count (find-by-extension root "md"))
              (count (find-by-extension root "org"))
              (count (find-by-extension root "pdf")))
           (count (find-by-extension root "md" "org" "pdf"))))
    (is (= 18 (count (find-by-extension root "md" "org" "pdf"))))
    (is (= 6 (count (find-by-extension root "md"))))
    (is (= 6 (count (find-by-extension root "org"))))
    (is (= 6 (count (find-by-extension root "pdf")))))
  (testing "Try to find files that don't exist."
    (is (= 0 (count (find-by-extension "odt"))))))

(deftest paths->files-test
  (testing "Convert files paths to File instances."
    (is (every? (partial instance? java.io.File)
                (paths->files (find-by-regexp root #".*"))))))

(deftest total-size-test
  (testing "More (identical) files means bigger size."
    (is (> (total-size (paths->files (find-by-extension root "flac" "mp3")))
           (total-size (paths->files (find-by-extension root "flac")))))))

(deftest copy-files-test
  (testing "Copy files from different sources to one destination."
    (let [dst-dir (create-dst-dir)]
      (->> (find-by-regexp root #".+")
           paths->files
           (copy-files dst-dir))
      (is (= 31 (count (find-by-regexp dst-dir #".+")))))))

(deftest clean-dir-test
  (testing "Delete directory with all subdirectories and files."
    (let [dir (create-dir-tree-for-delete)]
      (is (not (empty? (fs/list-dir dir))))
      (clean-dir dir)
      (is (empty? (fs/list-dir dir))))))
