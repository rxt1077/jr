(ns jr.util
  (:require [clojure.data.codec.base64 :as b64]))

(defn bytes->base64
  "recursively converts all byte arrays to base64 Strings"
  ([data]
    (if (bytes? data) (String. (b64/encode data))
      (if (seq? data) (map bytes->base64 data)
        (if (map? data) (reduce bytes->base64 {} data)
          data))))
  ([new-map [key val]]
    (assoc new-map key (bytes->base64 val))))
