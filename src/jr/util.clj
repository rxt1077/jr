(ns jr.util
  (:require [clojure.data.codec.base64 :as b64]))

(defn bytes->base64
  [data]
  "converts a byte array to base64 in a String"
  (String. (b64/encode data)))

(defn map->base64
  [msg]
  "converts byte arrays in a map to base64"
  (reduce (fn [new-map [key val]]
            (if (bytes? val)
              (assoc new-map key (bytes->base64 val))
              (assoc new-map key val)))
          {} msg))
