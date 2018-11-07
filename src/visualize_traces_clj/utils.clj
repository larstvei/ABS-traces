(ns visualize-traces-clj.utils
  (:require [quil.core :as q :include-macros true]))

(defn dotted-line
  ([x1 y1 x2 y2] (dotted-line x1 y1 x2 y2 10))
  ([x1 y1 x2 y2 d]
   (let [n (quot (q/dist x1 y1 x2 y2) d)]
     (dotimes [k n]
       (q/point (q/lerp x1 x2 (/ k n))
                (q/lerp y1 y2 (/ k n)))))))

(defn dotted-arrow
  ([x1 y1 x2 y2] (dotted-arrow x1 y1 x2 y2 10))
  ([x1 y1 x2 y2 d]
   (let [n (quot (q/dist x1 y1 x2 y2) d)
         l (/ (dec n) (max 1 n))]
     (dotted-line x1 y1 x2 y2 d)
     (q/push-matrix)
     (q/translate (q/lerp x1 x2 l) (q/lerp y1 y2 l))
     (q/rotate (q/atan2 (- y2 y1) (- x2 x1)))
     (q/triangle 0 0 -5 2.5 -5 -2.5)
     (q/pop-matrix))))

(defn label-line [text x1 y1 x2 y2]
  (q/push-matrix)
  (q/translate (q/lerp x1 x2 0.25) (q/lerp y1 y2 0.25))
  (if (< x1 x2)
    (q/rotate (q/atan2 (- y2 y1) (- x2 x1)))
    (q/rotate (q/atan2 (- y1 y2) (- x1 x2))))
  (q/text text 0 -5)
  (q/pop-matrix))
