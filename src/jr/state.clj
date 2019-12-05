(ns jr.state
  "all these functions modify the state of a node"
  (:require [clojure.java.io :as io])
  (:require [jr.message :as message])
  (:require [clojure.set :as cljset]))

(defn save-state
  "Saves the current state (keypair / known messages) to state-file"
  [state-file state]
  (spit state-file (print-str state)))

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
  :following messages"
  [pub-keys state]
  (let [keyp (:keyp state)
        msg-objs (map #(hash-map :following %) pub-keys)
        messages (map #(message/new-message keyp %) msg-objs)]
    (assoc state :following (cljset/union (:following state) pub-keys)
                 :messages (conj (:messages state) messages))))
