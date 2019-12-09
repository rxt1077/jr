(ns jr.node
  "all these functions change the data (state) of a node"
  (:require [jr.message :as message])
  (:require [jr.util :as util])
  (:require [clojure.set :as cljset])
  (:require [clojure.pprint :as pp]))

(defn pprint
  "Prints a pretty version of a node with base64 keys to an optional writer. If
  no writer is specified it defaults to *out*"
  ([node1] (pprint node1 *out*))
  ([node1 writer]
    (pp/pprint (util/map->base64 node1) writer)))

(defn save
  "Saves the current state of a node to node-file"
  [node1 node-file]
  (spit node-file (pprint node1)))

;(defn load-state
;  "Loads the current state from state-file"
;  [state-file state]
;  (if (.exists (io/file state-file))
;    (let [-state (slurp state-file)]
;      (def keyp (:keyp current-state))
;      (def messages (:messages current-state)))
;    (do
;      (println "No config file found, generating a new key...")
;      (def keyp id/new-key)
;      (save-state))))

(defn follow
  "Adds a set of public keys to the :following set and adds the
  :following messages. Returns an updated version of the node."
  [node1 pub-keys]
  (let [keyp (:keyp node1)
        msg-objs (map #(hash-map :following %) pub-keys)
        messages (map #(message/new-message keyp %) msg-objs)]
    (assoc node1 :following (cljset/union (:following node1) pub-keys)
                 :messages  (cljset/union (:messages  node1) messages))))

(defn sync-obj
  "Takes two nodes and modifies them as if node1 synced messages with node2.
  Returns an updated version of node1. This is mainly used for simulation."
  [node1 node2]
  (assoc node1 :messages (cljset/union (:messages node1)
    (filter
      #(cljset/subset? (set (:public %)) (:extended node1))
      (:messages node2)))))

(defn update-extended
  "Goes through :messages and adds public keys from :following messages to
  :extended. Returns a new version of node."
  [node1]
  (assoc node1 :extended (cljset/union (:extended node1)
    (map
      #(:following %)
      (filter #(contains? % :following) (:messages node1))))))
