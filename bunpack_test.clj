(require '[clojure.java.shell :refer [sh]])

(defn get-folder-contents [path] (reduce (fn [acc f] (assoc acc (.getName f) (slurp f))) {}
                                         (.listFiles (java.io.File. path))))

(def exemplar (get-folder-contents "test_resources/test_content"))

(defn rm-test-target [] (sh "rm" "-rf" "test_target"))

(defn test-unpacker [test-file]
  (rm-test-target)
  (let [{:keys [exit] :as command-output} (sh "bb" "-f" "bunpack.clj" test-file "test_target")]
    (when (< 0 exit)
      (println "bunpack failed attempting to unpack " test-file ":")
      (println command-output)
      (rm-test-target)
      (System/exit 1)))
  (when (not= exemplar (get-folder-contents "test_target/test_content"))
    (println "content folder does not match exemplar after unpacking " test-file)
    (System/exit 1)))

(test-unpacker "test_resources/targz_test.tar.gz")
(test-unpacker "test_resources/tarbz2_test.tar.bz2")
(test-unpacker "test_resources/tarxz_test.tar.xz")
(test-unpacker "test_resources/zip_test.zip")
(rm-test-target)
(println "All unpackers passed! 🐰")
