(ns jr.message
  (:require [jr.id :as id]
            [clojure.data.codec.base64 :as b64]))

(defn bytes->base64
  [data]
  "converts a byte array to base64 in a String"
  (String. (b64/encode data)))

(defn msg->base64
  [msg]
  "converts byte arrays in a message to base64"
  (reduce (fn [new-map [key val]]
            (if (bytes? val)
              (assoc new-map key (bytes->base64 val))
              (assoc new-map key val)))
          {} msg))

(defn new-message
  "creates a new message"
  [keyp msg]
  (let [signed-map (assoc msg :timestamp (inst-ms (java.util.Date.)))
        public (:public keyp)
        private (:private keyp)
        signature (id/sign private signed-map)]
    (assoc signed-map :public public :signature signature)))

(defn verify-message
  "verifies the signature of a message"
  [msg]
  (let [public (:public msg)
        signature (:signature msg)
        signed-map (dissoc msg :public :signature)]
    (id/verify public signature signed-map)))
