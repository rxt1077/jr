(ns jr.alias
  (:require [jr.util :as util]))

(defn bytes->alias
  "recursively converts all byte arrays to an alias String"
  [aliases data]
  (util/bytes->fn data #(get aliases %)))

(defn alias->public
  "Looks up a public key by its alias"
  [aliases lookup-alias]
    (some (fn [[key val]] (if (= val lookup-alias) key)) aliases))
