(require '[babashka.classpath :refer [add-classpath]]
         '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str])

(def deps '{:deps {docopt {:git/url "https://github.com/nubank/docopt.clj"
                           :sha "98814f559d2e50fdf10f43cbe3b7da0ca3cca423"}}})

(def cp (-> (sh "clojure" "-Spath" "-Sdeps" (str deps)) :out str/trim))
(add-classpath cp)
(require '[docopt.core :as docopt])

(def usage "Bunpack: never have to remember how to unpack your buns

Usage:
  bunpack <file>
  bunpack -t TARGET_PATH <file>
  bunpack -h | --help

Options:
  -h --help                        Show this screen.
  -t TARGET_PATH --to=TARGET_PATH  Extract to given path")

(defn strip-suffix [file suffix]
  (subs file 0 (- (count file) (count suffix))))

(defn untar [file dest compression]
  ["tar" compression "--extract" (str "--one-top-level=" dest) "-f" file])

(defn tar-gz [file destination]
  [(untar file (or destination (strip-suffix file ".tar.gz")) "-z")])

(defn tar-bz2 [file destination]
  [(untar file (or destination (strip-suffix file ".tar.bz2")) "-j")])

(defn tar-xz [file destination]
  (let [tar-file (strip-suffix file ".xz")]
    [["xz" "--decompress" "--keep" file]
     (untar tar-file (or destination (strip-suffix file ".tar.xz")) nil)
     ["rm" tar-file]]))

(defn zip [file destination]
  [["unzip" file "-d" (or destination (strip-suffix file ".zip"))]])

(defn printerr [output]
  (when-not (str/blank? output)
    (binding [*out* *err*]
      (println output))))

(defn run [{file "<file>", destination "--to"}]
  (let [get-cmd (fn [extractor-fn] (fn [_] (extractor-fn file destination)))
        commands (condp #(str/ends-with? %2 %1) file
                   ".tar.gz" :>> (get-cmd tar-gz)
                   ".tar.xz" :>> (get-cmd tar-xz)
                   ".tar.bz2" :>> (get-cmd tar-bz2)
                   ".zip" :>> (get-cmd zip)
                   ::unsupported)]
    (if (= commands ::unsupported)
      (do
        (printerr "File format not supported")
        (System/exit 1))
      (doseq [command commands
              :let [{:keys [exit out err]} (apply sh (filter some? command))]]
        (when (< 0 exit)
          (printerr (str "Error running '" (str/join " " command)
                         "', exited with status " exit "\n" err))
          (System/exit exit)))))
  (System/exit 0))

(docopt/docopt usage *command-line-args* (fn [opts] (if (get opts "--help")
                                                      (println usage)
                                                      (run opts))))
