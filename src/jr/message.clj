(ns jr.message
  (:require [jr.id :as id]))

(defn new-message
  "creates a new message"
  [public private msg]
  (let [signed-map (assoc msg :timestamp (inst-ms (java.util.Date.)))
        signature (id/sign private signed-map)]
    (assoc signed-map :public public :signature signature)))

(defn verify-message
  "verifies the signature of a message"
  [msg]
  (let [public (:public msg)
        signature (:signature msg)
        signed-map (dissoc msg :public :signature)]
    (id/verify public signature signed-map)))
