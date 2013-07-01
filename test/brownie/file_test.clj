(ns brownie.file-test
  (:require [clojure.test :refer :all]
            [brownie.file :refer :all]
            [me.raynes.fs :as fs]))

(defn create-test-dirs-files []
  (let [root (fs/temp-dir "brownie-")
        music-dir-name "music"
        docs-dir-name "docs"
        create-file (fn [dir name] (spit (fs/file root dir name) "tmp"))
        create-music-file (partial create-file music-dir-name)
        create-doc-file (partial create-file docs-dir-name)]
    (doseq [dir [music-dir-name docs-dir-name]] (fs/mkdir (fs/file root dir)))
    (doseq [file ["1.flac" "2.flac" "3.flac" "a.flac" "b.flac" "c.flac"
                  "1.mp3" "2.mp3" "3.mp3" "a.mp3" "b.mp3" "c.mp3"]]
      (create-music-file file))
    (doseq [file ["1.org" "2.org" "3.org" "a.org" "b.org" "c.org"
                  "1.pdf" "2.pdf" "3.pdf" "a.pdf" "b.pdf" "c.pdf"
                  "1.md" "2.md" "3.md" "a.md" "b.md" "c.md"]]
      (create-doc-file file))
    root))

(def root (create-test-dirs-files))

(deftest find-by-regexp-test
  (testing "Find file by part of name."
    (is (= 15 (count (find-by-regexp root #"\d+\..+"))))
    (is (= 15 (count (find-by-regexp root #"[a-zA-Z]\..+"))))
    (is (= 5 (count (find-by-regexp root #"1"))))
    (is (= 5 (count (find-by-regexp root #"b")))))
  (testing "Find file by name and extension."
    (is (= 3 (count (find-by-regexp root #"\d+\.f.+"))))
    (is (= 30 (count (find-by-regexp root #"\w+\..*"))))
    (is (= 12 (count (find-by-regexp root #"\w+\.m"))))
    (is (= 6 (count (find-by-regexp root #"\d+\.m"))))
    (is (= 3 (count (find-by-regexp root #"[a-zA-Z]+\.flac"))))
    (is (= 6 (count (find-by-regexp root #"\w+\.mp3")))))
  (testing "Find file by few regexps."
    (is (= 21 (count (find-by-regexp root #"\w+\.m.*" #"\d\."))))
    (is (= 9 (count (find-by-regexp root #"\.p" #"\.l" #"\d\.f")))))
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
