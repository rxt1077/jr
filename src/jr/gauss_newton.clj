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

(defn example-function
  "Example function from Wikipedia article"
  [s v k]
  (/ (* v s) (+ k s)))

(defn l5p
  "Five parameter logistic:
  https://www.mathworks.com/matlabcentral/fileexchange/38043-five-parameters-logistic-regression-there-and-back-again"
  [x a b c d e]
  (+ d (/ (- a d) (Math/pow (+ 1 (Math/pow (/ x c) b)) e))))

(defn generalized-logistic
  "Generalized logistic:
  https://en.wikipedia.org/wiki/Generalised_logistic_function"
  [t a k b v c m] 
  (+ a (/ (- k a) (Math/pow (+ c (Math/exp (* -1 b (- t m)))) (/ 1 v)))))

(defn residuals
  "Calculates the residuals, y_i - f(x_i), given a function and data"
 [estimates f data]
 (mapv #(let [[x y] %
              params (apply vector (cons x estimates))
              fx (apply f params)]
          (- y fx))
       data))

(defn ssr
  "Calculates the SSR from a matrix of residuals"
  [r]
  (reduce #(+ %1 (* %2 %2)) 0 r))

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

(defn scale-increment
  "Scales an increment by a specified alpha (learning rate)"
  [increment alpha]
  (mapv #(* % alpha) increment))

(defn possible-estimates
  "Calculates [ssr estimates] for all possible scaled increments" 
  [increment estimates f data]
  (mapv #(let [new-estimates (mapv + estimates (scale-increment increment %))
               ssr           (ssr (residuals new-estimates f data))]
           [ssr new-estimates])
        [1E2 1E1 1E0 1E-1 1E-2]))

(defn step
  "Calculates the next Gauss-Newton estimates"
  [estimates f data]
  (let [j                    (jacobian  estimates f data)
        jt                   (matrix/transpose j)
        r                    (residuals estimates f data)
        increment            (matrix/mmul (matrix/inverse (matrix/mmul jt j))
                                          (matrix/mmul jt r))
        possible             (possible-estimates increment estimates f data)
        [ssr next-estimates] (apply min-key first possible)]
    (printf "SSR: %f\nIncrement: %s\nNext Estimates: %s\n"
            ssr increment (pr-str next-estimates))
    next-estimates))

(defn gauss-newton
  "Returns an infinite series of Gauss-Newton steps"
  [estimates f data]
  (iterate #(step % f data) estimates))
