#!/usr/bin/env bb
(def usage "Bunpack: never have to remember how to unpack your buns

Unpacks file to destination. If destination not provided, will use file basename

Usage:
  bunpack <file> [<destination>] [options]

Options:
  -d --dry      Dry run, output commands that would be run
  -v --verbose  Show output from commands run
  -h --help     Show this screen. ")

(require '[babashka.classpath :refer [add-classpath]]
         '[clojure.java.shell :refer [sh]]
         '[clojure.string :as str])

(def deps '{:deps {docopt {:git/url "https://github.com/nubank/docopt.clj"
                           :sha "98814f559d2e50fdf10f43cbe3b7da0ca3cca423"}}})

(def cp (-> (sh "clojure" "-Spath" "-Sdeps" (str deps)) :out str/trim))
(add-classpath cp)
(require '[docopt.core :as docopt])

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

(defn printout [output]
  (when-not (str/blank? output)
    (println output)))

(defn get-commands [file destination]
  (letfn [(get-cmds-from [cmd-fn]
            (fn [_] (cmd-fn file destination)))]
    (condp #(str/ends-with? %2 %1) file
      ".tar.gz" :>> (get-cmds-from tar-gz)
      ".tar.xz" :>> (get-cmds-from tar-xz)
      ".tar.bz2" :>> (get-cmds-from tar-bz2)
      ".zip" :>> (get-cmds-from zip)
      ::unsupported)))

;; TODO: move System/exit calls into wrapper to make this usable as a pod?
(defn bunpack [file destination dry verbose]
   (let [commands (get-commands file destination)]
     (cond
       (= commands ::unsupported)
       (do
         (printerr "File format not supported")
         (System/exit 1))

       (true? dry)
       (doseq [command commands]
         (printout (str/join " " command)))

       :else
       (doseq [command commands
               :let [{:keys [exit out err]} (apply sh (filter some? command))
                     cmd-str (str/join " " command)]]
         (when verbose
           (printout cmd-str)
           (printout out))
         (when (< 0 exit)
           (printerr (str "Error running '" cmd-str
                          "', exited with status " exit "\n" err))
           (System/exit exit))))))

(docopt/docopt usage *command-line-args*
               (fn [{file "<file>", destination "<destination>",
                     dry "--dry", help "--help", verbose "--verbose"}]
                 (if help
                   (println usage)
                   (bunpack file destination dry verbose))))
