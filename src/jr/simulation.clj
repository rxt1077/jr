(ns jr.simulation
  (:require [jr.id :as id])
  (:require [jr.node :as node])
  (:require [jr.alias])
  (:require [clojure.set :as cljset]))

(defn create-node
  "Creates an empty node and returns it"
  []
  (let [[public private] (id/new-key)]
    {:public    public
     :private   private
     :messages  (set nil)
     :following (set nil)
     :extended  (set nil)}))
        
(defn create-nodes
  "Creates n empty nodes and returns them"
  [n]
  (repeatedly n create-node))

(defn public-keys
  "Gets a set of all public keys from a node list"
  [nodes]
  (set (map #(:public %) nodes)))

(defn rand-follow
  "Picks n random nodes to follow and has node follow them. node can't follow
  itself. Returns an updated node1."
  [nodes node1 n]
  (let [possible-keys (remove #(= (:public node1) %) (public-keys nodes))]
    (node/follow node1 (set (take n (shuffle possible-keys))))))

(defn net-bootstrap
  "Creates jr network with n nodes randomly following f nodes"
  [n f]
  (let [nodes (create-nodes n)]
    (map #(rand-follow nodes % f) nodes)))

(defn sync-obj
  "Takes two nodes and modifies them as if node1 synced messages with node2 and
  then updated its :extended set. Returns a new version of node1."
  [node1 node2]
  (node/update-extended 
    (assoc node1 :messages (cljset/union (:messages node1)
      (filter
        #(contains? (:extended node1) (:public %))
        (:messages node2))))))

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

(defn pprint
  "Pretty prints with aliases"
  [nodes aliases]
  (node/pprint nodes #(jr.alias/bytes->alias aliases %)))

(defn create-aliases
  "Makes up an alias list for a set of public keys. Up to 24 aliases are
  supported. Returns the list."
  [pub-keys]
  (let [names ["Alice" "Bob" "Carol" "David" "Eve" "Frank" "Greta" "Harry"
               "Irma" "Joseph" "Kathrine" "Leo" "Mary" "Norman" "Olivia"
               "Patrick" "Quanita" "Robert" "Samantha" "Tad" "Uma" "Vladimir"
               "Wanda" "Xavier" "Yolanda" "Zach"]]
    (zipmap pub-keys names)))

(defn lookup-by-key
  "Looks up a node in the network by its public key"
  [nodes public]
  (some #(if (= (:public %) public) %) nodes))

(defn lookup-by-alias
  "Looks up a node in the network by its alias"
  [nodes aliases lookup-alias]
  (lookup-by-key nodes (jr.alias/alias->public aliases lookup-alias)))
