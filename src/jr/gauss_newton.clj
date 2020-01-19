(ns jr.gauss-newton
  (:require [clojure.core.matrix :as matrix]))

(def small-amount 0.000000000001)

(def example-data ; [[S], Rate]
  [[0.038 0.050]
   [0.194 0.127]
   [0.425 0.094]
   [0.626 0.2122]
   [1.253 0.2729]
   [2.500 0.2665]
   [3.740 0.3317]])

(defn logistic
  "Five parameter logistic"
  [x a b c d e]
  (+ d (/ (- a d) (Math/pow (+ 1 (Math/pow (/ x c) b)) e))))

(defn example-function
  "Example function from Wikipedia article"
  [s v k]
  (/ (* v s) (+ k s)))

(defn residuals
  "Calculates the residuals, y_i - f(x_i), given a function and data"
 [estimates f data]
 (mapv #(let [[x y] %
              params (apply vector (cons x estimates))
              fx (apply f params)]
          (- y fx))
       data))

(defn partials
  "Calculates the partial differentials for each parameter in a function, f,
  using finite difference. The first parameter is the independent variable, x,
  and it's differential is not calculated. All other parameter differentials
  are."
  [params f]
  (let [h  small-amount
        fx (apply f params)]
    (mapv #(/ (- (apply f (assoc params % (+ (nth params %) h))) fx) h)
         (range 1 (count params)))))

(defn jacobian
  "Calculates the Jacobian of the vector of residuals"
  [estimates f data]
  (mapv (fn [pair]
          (let [[x y] pair
                params (apply vector (cons x estimates))]
            (partials params f)))
        data))

(defn step
  "Calculates the next Gauss-Newton estimates"
  [estimates f data]
  (let [j              (jacobian  estimates f data)
        jt             (matrix/transpose j)
        r              (residuals estimates f data)
;        ssr            (reduce #(+ %1 (* %2 %2)) 0 r)
        increment      (matrix/mmul (matrix/inverse (matrix/mmul jt j))
                                    (matrix/mmul jt r))
        next-estimates (mapv + estimates increment)]
    next-estimates))

(defn gauss-newton
  "Returns an infinite series of Gauss-Newton steps"
  [estimates f data]
  (iterate #(step % f data) estimates))
