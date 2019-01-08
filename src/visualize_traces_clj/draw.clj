(ns visualize-traces-clj.draw
  (:require [quil.core :as q :include-macros true]
            [visualize-traces-clj.dpor :refer [enabled-by trace->history]]
            [visualize-traces-clj.event-keys :refer :all]
            [visualize-traces-clj.utils :refer :all]))

(defn setup [trace]
  (q/color-mode :hsb)
  (q/text-align :center :center)
  (q/text-font (q/create-font "monospace" 12))
  (q/stroke-weight 2)
  (let [history (trace->history trace)]
    {:trace trace
     :cogs (keys trace)
     :history history
     :start 0
     :height (min (count history) 10)}))

(def event-color
  (memoize
   (let [color (atom 0)]
     (fn [event-key]
       (swap! color (comp #(mod % 256) (partial + 112)))
       [@color 150 256]))))

(defn draw-grid [n m wd hd]
  (q/no-stroke)
  (let [width (- (q/width) (* 2 wd))]
    (dotimes [h m]
      (let [y1 (+ (/ hd 2) (* h hd))]
        (q/fill (if (even? h) 245 250))
        (q/rect wd y1 width hd)))))

(defn draw-cogs [cogs wd hd]
  (q/fill 0)
  (doseq [cog cogs]
    (let [x (* wd (inc (.indexOf cogs cog)))
          y (/ hd 3)]
      (q/text (str cog) x y))))

(defn draw-events [trace history cogs wd hd]
  (doseq [[i events] (map-indexed vector history)]
    (doseq [[cog id] events]
      (let [type (event-key-type trace [cog id])
            task (event-key-task trace [cog id])
            method (event-key-method-name trace [cog id])
            j (.indexOf cogs cog)
            x (* wd (inc j))
            y (+ (* (inc i) hd)
                 (if (even? j) (- (/ hd 8)) (/ hd 8)))
            tl (/ (q/text-width method) 2)
            tx (cond (neg? (- x tl)) (+ x (/ tl 2))
                     (> (+ x tl) (q/width)) (- x (/ tl 2))
                     :else x)]
        (apply q/fill (event-color task))
        (q/no-stroke)
        (q/ellipse x y 15 15)
        (q/fill 0)
        (q/text method tx (- y (/ hd 4)))
        (q/text (name type) x (+ y (/ hd 4)))
        (q/stroke 0)
        (doseq [[cog2 id2] (enabled-by [cog id] trace)]
          (let [k (.indexOf cogs cog2)
                l (count (take-while (complement #(% [cog2 id2])) history))
                x2 (* wd (inc k))
                y2 (+ (* (inc l) hd)
                      (if (even? k) (- (/ hd 8)) (/ hd 8)))
                method (event-key-method-name trace [cog id])]
            (if (= cog cog2)
              (let [n (quot (q/dist x y x2 y2) 10)]
               (dotimes [k (- n 2)]
                 (q/point (- x (* (/ wd 2) (q/sin (* (/ k n) q/PI))))
                          (q/lerp y y2 (/ (inc k) n))))
               (dotted-arrow (- x (* (/ wd 2) (q/sin (* (/ (- n 2) n) q/PI))))
                             (q/lerp y y2 (/ (dec n) n))
                             x2 y2))
              (dotted-arrow x y x2 y2))))))))

(defn draw-state [state]
  (q/background 255)
  (let [history (take (:height state) (drop (:start state) (:history state)))
        n (count (:cogs state))
        m (count history)
        wd (/ (q/width) (inc n))
        hd (/ (q/height) (inc m))]
    (draw-grid n m (/ wd 2) hd)
    (draw-cogs (:cogs state) wd hd)
    (draw-events (:trace state) history (:cogs state) wd hd))
  (q/no-loop))
