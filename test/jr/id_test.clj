(ns jr.id-test
  (:require [clojure.test :refer :all]
            [jr.id :refer :all]))

(deftest signatures
  "Generates a key pair, uses it to sign an object, and then checks that the
  signature is valid"
  (let [keyp    (new-key)
        data    {:test "testing"}
        public  (:public keyp)
        private (:private keyp)
        signature (sign private data)]
    (is (verify public signature data))))
