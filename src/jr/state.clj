(ns jr.state
  (:require [clojure.java.io :as io])
  (:require [jr.id :as id]))

(def state-file "~/.jr.edn")
(def keyp {})
(def messages #{})

(defn save-state
  "Saves the current state (keypair / known messages) to ~/.jr.edn"
  []
  (spit "~/.jr.edn" (print-str {:keyp keyp :messages messages})))

(defn load-state
  "Loads the current state from ~/.jr.edn"
  []
  (if (.exists (io/file state-file))
    (let [current-state (slurp "~/.jr.edn")]
      (def keyp (:keyp current-state))
      (def messages (:messages current-state)))
    (do
      (println "No config file found, generating a new key...")
      (def keyp id/new-key)
      (save-state))))
