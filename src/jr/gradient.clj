(ns jr.gradient
  (:require [clojure.data.csv :as csv])
  (:require [clojure.java.io :as io]))

(defn read-csv
  "Reads csv file into a list as [iterations, column values]"
  [file col]
  (with-open [reader (io/reader file)]
    (map #(let [iteration (first %)
                value (nth % col)]
            [(Integer/parseInt iteration), (Double/parseDouble value)])
         (drop 1 (doall (csv/read-csv reader))))))

(defn calc-ak
  "Returns the lower and upper asymptotic limits, [a k], for a network given
  the number of nodes, n, and the number of nodes being followed, f"
  [n f]
  (let [a         f
        f-squared (* f f)]
    [a (if (< n f-squared) n f-squared)]))

(defn exp
  "Returns e^{-B(t-M)} which is used quite a bit in these equations"
  [t b m]
  (Math/exp (* -1 b (- t m))))

(defn logistic
  "Returns the output of a logistic function for given t using a, k, b, and m."
  ([t a k b m]
   (logistic t a k b m (exp t b m)))
  ([t a k b m calculated-exp]
   (+ a (/ (- k a) (+ 1 calculated-exp)))))

(defn diff
  "Returns the difference between the actual output, y, and the calculated
  logistic"
  ([t a k b m y]
   (diff t a k b m y (exp t b m)))
  ([t a k b m y calculated-exp]
   (- y (logistic t a k b m calculated-exp))))

(defn ssr
  "Returns the average sum of the squares of the residuals"
  [a k b m data]
  (/
   (reduce #(+ %1 (let [[t y] %2] (Math/pow (diff t a k b m y) 2))) 0 data)
   (count data)))

(defn graph-ssr
  "Creates data/ssr.csv for graphing the cost function (sum of the squares of
  the residuals)"
  [a k data]
  (with-open [writer (io/writer "data/ssr.csv")]
    (let [b-range (range 0.0 0.1 0.01M)
          m-range (range 0 1000 10)]
      (csv/write-csv writer (cons ["b" "m" "ssr"]
        (for [b b-range m m-range] 
          [b m (ssr a k b m data)]))))))

(defn pdb
  "Returns the partial derivative of the logistic function with respect to b"
  ([t a k b m]
   (pdb t a k b m (exp t b m)))
  ([t a k b m calculated-exp]
   (/ (* (- k a) (- t m) calculated-exp) (Math/pow (+ 1 calculated-exp) 2))))

(defn pdm
  "Returns the partial derivative of the logistic function with respect to m"
  ([t a k b m]
   (pdm t a k b m (exp t b m)))
  ([t a k b m calculated-exp]
   (/ (* -1 (- k a) b calculated-exp) (Math/pow (+ 1 calculated-exp) 2))))

(defn grad
  "Returns the gradient of the cost function as [grad-b grad-m]"
  [a k b m data]
  (map #(/ % (count data))
    (reduce
      #(let [[t y] %2
             calculated-exp (exp t b m)
             difference (diff t a k b m y calculated-exp)
             partial-b (pdb t a k b m calculated-exp)
             partial-m (pdm t a k b m calculated-exp)
             [curr-b curr-m] %1]
         [(+ curr-b (* -2 partial-b difference))
          (+ curr-m (* -2 partial-m difference))])
      [0 0] data)))

(defn step
  "Performs a gradient descent step to determine the next b and m value that
  best fits the logistic function."
  ([a k b m eta-b eta-m data]
   (let [[grad-b grad-m] (grad a k b m data)]
     (step a k b m eta-b eta-m data grad-b grad-m)))
  ([a k b m eta-b eta-m data grad-b grad-m]
   (let [new-b (- b (* grad-b eta-b))
         new-m (- m (* grad-m eta-m))]
     [new-b new-m])))

(defn best-step
  "Performs a series of gradient descent steps at different learning rates to
  determine what the best step would be"
  [a k b m data]
  (let [eta-range [1E0 1E-1 1E-2 1E-3 1E-4 1E-5 1E-6 1E-7 1E-8 1E-9]]
    (apply min-key first
           (let [[grad-b grad-m] (grad a k b m data)]
             (for [eta-b eta-range eta-m eta-range]
               (let [[new-b new-m] (step a k b m eta-b eta-m data grad-b grad-m)
                     new-ssr       (ssr a k new-b new-m data)]
               [new-ssr new-b new-m]))))))

(defn descent
  "Returns an lazy infinite sequence of adaptive eta gradient descent steps"
  [a k start-b start-m data]
  (iterate #(let [[prev-ssr b m] %] (best-step a k b m data))
           [(ssr a k start-b start-m data) start-b start-m]))

(defn descent-to
  "Realizes results from the descent sequence until the ssr changes by less than
  diff. Returns [ssr b m]"
  [a k start-b start-m data diff]
  (loop [results (descent a k start-b start-m data)]
    (let [[ssr1 b1 m1] (first  results)
          [ssr2 b2 m2] (second results)]
      (if (< (- ssr1 ssr2) diff)
        [ssr2 b2 m2]
        (recur (rest results))))))
