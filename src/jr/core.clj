(ns jr.core
  (:require [jr.id :as id])
  (:require [jr.message :as message])
  (:require [jr.node :as node])
  (:require [jr.simulation :as sim])
  (:require [jr.alias])
  (:require [jr.gradient :as gradient])
  (:require [clojure.data.csv :as csv])
  (:require [clojure.java.io :as io])
  (:require [jr.gauss-newton :as gn]))

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

(defn avg-key-growth
  "Performs sim/rand-sync repeatedly to build a sequence showing how a
  particular key grows over time"
  [nodes searchkey iterations]
  (map #(float (sim/avg-key % searchkey))
       (take iterations (iterate sim/rand-sync nodes))))

(defn extended-growth
  "Determines how the exteneded network grows over random iterations."
  []
  (def nodes (sim/net-bootstrap 100 10))
  (with-open [writer (io/writer "data/extended_growth.csv")]
    (csv/write-csv writer
     (cons
       ["iterations" "avg_extended_size"]
        (map vector
          (range 1000) 
          (map #(float (sim/avg-key % :extended))
               (take 1000 (iterate sim/rand-sync nodes))))))))

(defn multi-extended-growth
  "Measures extended network growth for varying f values. Outputs
  data/multi_extended_growth.csv"
  [f-values]
  (with-open [writer (io/writer "data/multi_extended_growth.csv")]
    (csv/write-csv writer
      (let [labels (cons "iterations" (map #(format "f=%d" %) f-values))]
        (cons
          labels
          (apply mapv vector
                 (range 0 3000 1)
                 (map #(let [f %
                             nodes (sim/net-bootstrap 100 f)
                             data (avg-key-growth nodes :extended 3000)]
                         data)
                      f-values)))))))

(defn multi-descent
  "Determines the b and m values that best fit the columns in
  data/multi_extended_growth.csv and writes output to
  data/multi_descent.csv"
  [f-values]
  (with-open [writer (io/writer "data/multi_descent.csv")]
    (csv/write-csv
      writer
      (cons
        ["f" "b" "m"]
        (map-indexed #(let [f         %2
                            [a k]     (gradient/calc-ak 100 f)
                            data      (gradient/read-csv
                                        "data/multi_extended_growth.csv"
                                        (+ %1 1))
                            [ssr b m] (gradient/descent-to
                                        a k 0.01 250 data 0.001)]
                        [f b m])
                     f-values)))))
