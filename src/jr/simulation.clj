(ns jr.simulation
  (:require [jr.id :as id])
  (:require [jr.state :as state])
  (:require [clojure.set]))

(defn create-nodes
  "Creates n nodes with empty messages, following, and extended sets"
  [n]
  (repeatedly n (fn [] (hash-map :keyp (id/new-key)
                                 :messages (set nil)
                                 :following (set nil)
                                 :extended (set nil)))))

(defn public-key
  "Pulls the public key from a node"
  [node]
  (:public (:keyp node)))

(defn public-keys
  "Gets a set of all public keys from a node list"
  [nodes]
  (set (map public-key nodes)))

(defn rand-follow
  "Picks n random nodes to follow and has node follow them. node can't follow
  itself"
  [nodes node n]
  (let [possible-keys (clojure.set/difference (public-keys nodes) #{(public-key node)})]
    (state/follow (take n (shuffle possible-keys)) node)))

(defn net-bootstrap
  "Creates jr network with n nodes randomly following f nodes"
  [n f]
  (let [nodes (create-nodes n)]
    (map #(rand-follow nodes % f) nodes))) 
