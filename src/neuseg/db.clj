(ns neuseg.db
  (:use [clj-tuple]
        [clojure.core.matrix]))

(set-current-implementation :vectorz)

(def registry
  { "unigram"   (atom {})
    "bigram"    (atom {})
    "trigram"   (atom {})
    "quadgram"  (atom {}) })

(defn- splited-line [line]
  (let [items (clojure.string/split line #"\s+")
        wd    (first items)
        nvec  (normalise (vec (map #(Double. %1) (rest items))))]
        (tuple wd nvec)))

(defn- reg-wd-neglect [name data]
  (let [[[wd nvec] idx] data]
      (do
        (if (= (mod idx 10000) 0) (print "."))
        (swap! (get registry name) assoc wd idx)
        nvec)))

(defn load-db [name]
  (with-open [rdr (clojure.java.io/reader (str "data" "/" name))]
    (do
      (println "")
      (println "loading" name "started!")
      (let [last (atom -1)]
        (matrix (map (partial reg-wd-neglect name)
             (partition-by #(if (number? %1) (reset! last %1) (+ @last 1))
               (interleave (map splited-line (rest (line-seq rdr)))
                           (iterate inc 0)))))))))

(def unigram  (load-db "unigram"))
(def bigram   (load-db "bigram"))
(def trigram  (load-db "trigram"))
(def quadgram (load-db "quadgram"))
