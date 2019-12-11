(ns jr.core
  (:require [jr.id :as id])
  (:require [jr.message :as message])
  (:require [jr.node :as node])
  (:require [jr.simulation :as sim])
  (:require [jr.alias]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn setup
  "Just a quick set up so I don't have to keep retyping"
  []
  (println "=== Initial Setup ===")
  (def nodes1 (sim/create-nodes 3))
  (def aliases (sim/create-aliases (sim/public-keys nodes1)))
  (sim/pprint nodes1 aliases)

  (println "=== 1st follows 2nd and 2nd follows 3rd ===")
  (def nodes2
    (list (node/follow (first  nodes1) #{(:public (second nodes1))})
          (node/follow (second nodes1) #{(:public (nth nodes1 2))})
          (nth nodes1 2)))
  (sim/pprint nodes2 aliases)

  (println "=== 1st syncs with 2nd ===")
  (def nodes3
    (list (sim/sync-obj (first nodes2) (second nodes2))
          (second nodes2)
          (nth nodes2 2)))
  (sim/pprint nodes3 aliases))
