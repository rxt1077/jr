; Based on: https://gist.github.com/mikeananev/0b44ab0fb2b8635c146c3e45227b826c

(ns jr.id
  (:require [clojure.string :as str])
  (:import (java.security SecureRandom)
           (org.bouncycastle.crypto.generators Ed25519KeyPairGenerator)
           (org.bouncycastle.crypto.params Ed25519KeyGenerationParameters Ed25519PrivateKeyParameters Ed25519PublicKeyParameters)
           (org.bouncycastle.crypto.signers Ed25519Signer)))

(defn new-key
  "Creates a keypair as bytes in a map"
  []
  (let [random   (SecureRandom.)
        kpg      (Ed25519KeyPairGenerator.)
        _        (.init kpg (Ed25519KeyGenerationParameters. random))
        key-pair (.generateKeyPair kpg)]
    [(.getEncoded (.getPublic key-pair)) (.getEncoded (.getPrivate key-pair))]))

(defn new-signer
  "return new instance of `Ed25519Signer` initialized by private key bytes"
  [private]
  (let [signer (Ed25519Signer.)
        params (Ed25519PrivateKeyParameters. private 0)]
    (.init signer true params)
    signer))

(defn sign
  "generate a bytes signature for the data, which must be convertible to edn"
  [private data]
  (let [signer (new-signer private)
        data-bytes (.getBytes (print-str data))]
    (.update signer data-bytes 0 (alength data-bytes))
    (.generateSignature signer)))

(defn new-verifier
  "return new instance of `Ed25519Signer` initialized by public key bytes"
  [public]
  (let [signer (Ed25519Signer.)
        params (Ed25519PublicKeyParameters. public 0)]
    (.init signer false params)
    signer))

(defn verify
  "verify signature for a data map"
  [public signature data]
  (let [signer (new-verifier public)
        data-bytes (.getBytes (print-str data))]
    (.update signer data-bytes 0 (alength data-bytes))
    (.verifySignature signer signature)))
