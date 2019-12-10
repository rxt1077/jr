(ns jr.core
  (:require [jr.id :as id])
  (:require [jr.message :as message])
  (:require [jr.node :as node])
  (:require [jr.simulation :as sim]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn setup
  "Just a quick set up so I don't have to keep retyping"
  []
  (def nodes (sim/create-nodes 10))
  (def nodes (rand-nth nodes))
  (def node1 (sim/rand-follow nodes node1 1)))
