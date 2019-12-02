(defproject jr "0.1.0-SNAPSHOT"
  :description "Peer-to-peer social message exchange based on Secure Scuttlebutt"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.bouncycastle/bcprov-jdk15on "1.64"]
                 [org.clojure/data.codec "0.1.1"]]
  :main ^:skip-aot jr.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
