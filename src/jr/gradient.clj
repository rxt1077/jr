(ns jr.gradient
  (:require [clojure.data.csv :as csv])
  (:require [clojure.java.io :as io]))

(defn read-csv
  "Reads data/extended_setup.csv into a list"
  []
  (with-open [reader (io/reader "data/extended_setup.csv")]
    (map #(let [[t y] %] [(Integer/parseInt t) (Double/parseDouble y)])
         (drop 1 (doall (csv/read-csv reader))))))

(defn exp
  "Returns e^{-B(t-M)} which is used quite a bit in these equations"
  [t b m]
  (Math/exp (* -1 b (- t m))))

(defn denom
  "Returns 1 + e^{-B(t-M)} which is often used as a denominator"
  [t b m]
  (+ 1 (exp t b m)))

(defn logistic
  "Returns the output of a logistic function for given t using f, b, and m.
  Where f is the number of followers each node starts with, b is the growth
  rate, and m is the time of maximum growth."
  [t f b m]
  (+ f (/ (* f (- f 1)) (denom t b m))))

(defn diff
  "Returns the difference between the actual output, y, and the calculated
  logistic using t, f, b, and m."
  [t f b m y]
  (- y (logistic t f b m)))

(defn ssr
  "Returns the average sum of the squares of the residuals"
  [f b m data]
  (/
   (reduce #(+ %1 (let [[t y] %2] (Math/pow (diff t f b m y) 2))) 0 data)
   (count data)))

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

(defn pdb
  "Returns the partial derivative of the logistic function with respect to b"
  [t f b m]
  (/ (* f (- f 1) (- t m) (exp t b m)) (Math/pow (denom t b m) 2)))

(defn pdm
  "Returns the partial derivative of the logistic function with respect to m"
  [t f b m]
  (/ (* -1 f (- f 1) b (exp t b m)) (Math/pow (denom t b m) 2)))

(defn grad
  "Returns the gradient of the cost function as [grad-b grad-m]"
  [f b m data]
  (map #(/ % (count data))
    (reduce
      #(let [[t y] %2
              difference (diff t f b m y)
              partial-b (pdb t f b m)
              partial-m (pdm t f b m)
              [curr-b curr-m] %1]
;         (printf "partial-b %f partial-m %f difference %f\n" partial-b partial-m difference)
         [(+ curr-b (* -2 partial-b difference))
          (+ curr-m (* -2 partial-m difference))])
      [0 0] data)))

(defn step
  "Performs a gradient descent step to determine the next b and m value that
  best fits the logistic function."
  [f b m eta-b eta-m data]
    (let [[grad-b grad-m] (grad f b m data)
          new-b           (- b (* grad-b eta-b))
          new-m           (- m (* grad-m eta-m))]
      ;(println "=== Gradient Descent Step ===")
      ;(printf "Input B=%f M=%f eta-b=%f eta-m=%f ssr=%f\n" b m eta-b eta-m
      ;        (ssr f b m data))
      ;(printf "Gradient: B=%f M=%f\n", grad-b, grad-m)
      ;(printf "Output B=%f M=%f ssr=%f\n" new-b new-m (ssr f new-b new-m data))
      [new-b new-m]))

(defn best-step
  "Performs a series of gradient descent steps at different learning rates to
  determine what the best step would be"
  [f b m data]
  (let [data      (read-csv)
        eta-range [1E0 1E-1 1E-2 1E-3 1E-4 1E-5 1E-6 1E-7 1E-8 1E-9]]
    (apply min-key first
           (for [eta-b eta-range eta-m eta-range]
             (let [[new-b new-m] (step f b m eta-b eta-m data)
                   new-ssr       (ssr f new-b new-m data)]
               [new-ssr new-b new-m])))))

(defn descent
  "Returns an lazy infinite sequence of adaptive eta gradient descent steps"
  [f start-b start-m data]
  (iterate #(let [[prev-ssr b m] %] (best-step f b m data))
           [(ssr f start-b start-m data) start-b start-m]))

(defn descent-to
  "Realizes results from the descent sequence until the ssr changes by less than
  diff. Returns [ssr b m]"
  [f start-b start-m data diff]
  (loop [results (descent f start-b start-m data)]
    (let [[ssr1 b1 m1] (first  results)
          [ssr2 b2 m2] (second results)]
;      (println ssr1)
;      (println b1)
;      (println m1)
;      (println ssr2)
;      (println b2)
;      (println m2)
      (if (< (- ssr1 ssr2) diff)
        [ssr2 b2 m2]
        (recur (rest results))))))
