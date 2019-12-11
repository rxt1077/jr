(ns jr.node
  "all these functions change the data (state) of a node"
  (:require [jr.message :as message])
  (:require [jr.util :as util])
  (:require [clojure.set :as cljset])
  (:require [clojure.pprint :as pp]))

(defn pprint
  "Prints a pretty version of a node with an optional conversion function for
  bytes and an optional writer. If no writer is specified it defaults to *out*.
  If no conversion function is specified it defaults to bytes->base64."
  ([node1] (pprint node1 util/bytes->base64))
  ([node1 convfn] (pprint node1 convfn *out*))
  ([node1 convfn writer]
    (pp/pprint (convfn node1) writer)))

(defn save
  "Saves the current state of a node to node-file"
  [node1 node-file]
  (spit node-file (pprint node1)))

(defn update-extended
  "Goes through :messages and adds public keys from :following messages to
  :extended. Returns a new version of the node."
  [node1]
  (assoc node1 :extended (cljset/union (:extended node1)
    (set (map
      #(:following %)
      (filter #(contains? % :following) (:messages node1)))))))

(defn follow
  "Adds a set of public keys to the :following set, adds the :following
  messages, and updates :extended. Returns an updated version of the node."
  [node1 pub-keys]
  (let [public   (:public node1)
        private  (:private node1)
        msg-objs (map #(hash-map :following %) pub-keys)
        messages (set (map #(message/new-message public private %) msg-objs))
        node1    (assoc node1
                   :following (cljset/union (:following node1) pub-keys)
                   :messages  (cljset/union (:messages  node1) messages))]
    (update-extended node1)))
