(ns jr.gradient
  (:require [clojure.data.csv :as csv])
  (:require [clojure.java.io :as io]))

(defn logistic
  "Returns the output of a logistic function for given t using f, b, and m.
  Where f is the number of followers each node starts with, b is the growth
  rate, and m is the time of maximum growth."
  [t f b m]
  (let [numerator (* f (- f 1))
        denominator (+ 1 (Math/exp (* (* -1 b) (- t m))))]
    (+ f (/ numerator denominator))))

(defn sr
  "Returns the square of the difference between the actual output, y, and the
  calculated logistic."
  [t f b m y]
  (Math/pow (- y (logistic t f b m)) 2))

(defn ssr
  "Returns the sum of the squares of the residuals"
  [f b m data]
  (reduce #(+ %1 (let [[t y] %2] (sr t f b m y))) 0 data))

(defn graph-ssr
  "Creates a graph of the cost function (sum of the squares of the residuals)
  for the output in data/extended_setup.csv"
  []
  (with-open [reader (io/reader "data/extended_setup.csv")
              writer (io/writer "data/ssr.csv")]
    (let [data (map #(let [[t y] %] [(Integer/parseInt t) (Double/parseDouble y)])
                    (drop 1 (doall (csv/read-csv reader))))
          b-range (range 0 0.1 0.01M)
          m-range (range 0 1000 10)]
      (csv/write-csv writer (cons ["b" "m" "ssr"]
        (for [b b-range m m-range] 
          [b m (ssr 10 b m data)]))))))
