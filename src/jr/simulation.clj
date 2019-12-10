(ns jr.simulation
  (:require [jr.id :as id])
  (:require [jr.node :as node])
  (:require [clojure.set :as cljset]))

(defn create-nodes
  "Creates n nodes with empty messages, following, and extended sets"
  [n]
  (repeatedly n (fn [] (hash-map :keyp (id/new-key)
                                 :messages (set nil)
                                 :following (set nil)
                                 :extended (set nil)))))

(defn public-key
  "Pulls the public key from a node"
  [node1]
  (:public (:keyp node1)))

(defn public-keys
  "Gets a set of all public keys from a node list"
  [nodes]
  (set (map public-key nodes)))

(defn rand-follow
  "Picks n random nodes to follow and has node follow them. node can't follow
  itself"
  [nodes node1 n]
  (let [possible-keys (cljset/difference (public-keys nodes) #{(public-key node1)})]
    (node/follow node1 (take n (shuffle possible-keys)))))

(defn net-bootstrap
  "Creates jr network with n nodes randomly following f nodes"
  [n f]
  (let [nodes (create-nodes n)]
    (map #(rand-follow nodes % f) nodes)))

(defn sync-obj
  "Takes two nodes and modifies them as if node1 synced messages with node2.
  Returns an updated version of node1. Returns a new version of node1."
  [node1 node2]
  (assoc node1 :messages (cljset/union (:messages node1)
    (filter
      #(contains? (set (:extended node1)) (:public %)) ;; shouldn't :extended already be a set?
      (:messages node2)))))

(defn rand-sync
  "Randomly picks 2 nodes and syncs them with each other. Returns an updated
  nodes"
  [nodes]
  (let [node-indexes (take 2 (shuffle (range (count nodes))))
        n1 (nth node-indexes 0)
        n2 (nth node-indexes 1)
        node1 (nth nodes n1)
        node2 (nth nodes n2)
        new-node1 (sync-obj node1 node2)
        new-node2 (sync-obj node2 node1)]
    (printf "Syncing nodes %d->%d and %d->%d\n" n1 n2 n2 n1)
    ; nodes is a lazy-seq, this keeps it that way
    (map-indexed #(if (= %1 n1) new-node1 (if (= %1 n2) new-node2 %2)) nodes)))

(defn sum-key
  "Sums up the count of items in a particular key" 
  [nodes key]
  (reduce #(+ %1 (count (key %2))) 0 nodes))

(defn avg-key
  "Returns the average amount items in a particular key"
  [nodes key]
  (/ (sum-key nodes key) (count nodes)))

(defn print-stats
  "Prints out statistics about a jr network"
  [nodes]
  (printf "=== %d nodes ===\n" (count nodes))
  (printf "Averages\n")
  (printf "  :following %d\n" (avg-key nodes :following))
  (printf "  :extended  %d\n" (avg-key nodes :extended))
  (printf "  :messages  %d\n" (avg-key nodes :messages)))
