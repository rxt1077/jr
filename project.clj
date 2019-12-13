(defproject jr "0.1.0-SNAPSHOT"
  :description "Peer-to-peer social message exchange based on Secure Scuttlebutt"
  :url "https://git.sr.ht/~bosco/jr"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.bouncycastle/bcprov-jdk15on "1.64"]
                 [org.clojure/data.codec "0.1.1"]
                 [org.clojure/tools.namespace "0.3.1"]
                 [org.clojure/data.csv "0.1.4"]]
  :main ^:skip-aot jr.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
