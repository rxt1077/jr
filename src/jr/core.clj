(ns jr.core
  (:require [jr.id :as id])
  (:require [jr.message :as message])
  (:require [jr.node :as node])
  (:require [jr.simulation :as sim])
  (:require [jr.alias])
  (:require [jr.gradient :as gradient])
  (:require [clojure.data.csv :as csv])
  (:require [clojure.java.io :as io]))

(defn extended-sync-test
  "Tests extended network syncing"
  []
  (println "=== Alice, Bob, and Carol create identities ===")
  (def nodes1 (sim/create-nodes 3))
  (def aliases (sim/create-aliases (sim/public-keys nodes1)))
  (sim/pprint nodes1 aliases)

  (println "=== Alice follows Bob and Bob follows Carol ===")
  (def nodes2
    (list (node/follow (first  nodes1) #{(:public (second nodes1))})
          (node/follow (second nodes1) #{(:public (nth nodes1 2))})
          (nth nodes1 2)))
  (sim/pprint nodes2 aliases)

  (println "=== Alice syncs with Bob ===")
  (def nodes3
    (list (sim/sync-obj (first nodes2) (second nodes2))
          (second nodes2)
          (nth nodes2 2)))
  (sim/pprint nodes3 aliases))

(defn extended-setup-test
  "Determines how many random iterations it takes to set up the extended
  network."
  []
  (def nodes (sim/net-bootstrap 100 10))
  (with-open [writer (io/writer "data/extended_setup.csv")]
    (csv/write-csv writer
     (cons
       ["iterations" "avg_extended_size"]
        (map vector
          (range 1000) 
          (map #(float (sim/avg-key % :extended))
               (take 1000 (iterate sim/rand-sync nodes))))))))

(defn gradient-test
  [b m]
  (with-open [reader (io/reader "data/extended_setup.csv")]
    (let [data (map #(let [[t y] %] [(Integer/parseInt t) (Double/parseDouble y)])
                    (drop 1 (doall (csv/read-csv reader))))]
      (gradient/ssr 10 b m data))))
;      (gradient/ssr f b m data) 

(defn ranges
  "Testing ranges"
  []
  (with-open [reader (io/reader "data/extended_setup.csv")]
    (def data (map #(let [[t y] %] [(Integer/parseInt t) (Double/parseDouble y)])
                    (drop 1 (doall (csv/read-csv reader))))))
  (def b-range (range 0 0.105 0.01M))
  (def m-range (range 0 1000.5 1))
  (for [b b-range m m-range] (println b m (gradient/ssr 10 b m data))))
