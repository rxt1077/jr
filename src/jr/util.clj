(ns jr.util
  (:require [clojure.data.codec.base64 :as b64]))

(defn bytes->fn
  "recursively converts all byte arrays via a function"
  ([data convfn]
    (if (bytes? data) (convfn data)
      (if (set? data) (set (map #(bytes->fn % convfn) data))
        (if (or (seq? data) (vector? data)) (map #(bytes->fn % convfn) data)
          (if (map? data) (reduce #(bytes->fn %1 %2 convfn) {} data)
            data)))))
  ([new-map [key val] convfn]
    (assoc new-map key (bytes->fn val convfn))))

(defn bytes->base64
  "recursively converts all byte arrays to base64 Strings"
  [data]
  (bytes->fn data #(String. (b64/encode %))))
