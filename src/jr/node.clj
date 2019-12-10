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
    (pp/pprint (util/bytes->base64 node1) writer)))

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

(defn update-extended
  "Goes through :messages and adds public keys from :following messages to
  :extended. Returns a new version of the node."
  [node1]
  (assoc node1 :extended (cljset/union (:extended node1)
    (map
      #(:following %)
      (filter #(contains? % :following) (:messages node1))))))

(defn follow
  "Adds a set of public keys to the :following set, adds the :following
  messages, and updates :extended. Returns an updated version of the node."
  [node1 pub-keys]
  (let [keyp (:keyp node1)
        msg-objs (map #(hash-map :following %) pub-keys)
        messages (map #(message/new-message keyp %) msg-objs)
        node1    (assoc node1
                   :following (cljset/union (:following node1) pub-keys)
                   :messages  (cljset/union (:messages  node1) messages))]
    (update-extended node1)))
