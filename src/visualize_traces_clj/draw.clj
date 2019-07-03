(ns visualize-traces-clj.draw
  (:require [quil.core :as q :include-macros true]
            [visualize-traces-clj.dpor :refer [enabled-by trace->history potential-deadlocks]]
            [visualize-traces-clj.event-keys :refer :all]
            [visualize-traces-clj.utils :refer :all]))

(defn make-state [trace]
  (let [history (trace->history trace)]
    {:trace trace
     :cogs (keys trace)
     :history history
     :deadlocks (potential-deadlocks trace)}))

(defn setup [traces]
  (q/frame-rate 10)
  (q/color-mode :hsb)
  (q/text-align :center :center)
  (q/text-font (q/create-font "monospace" 12))
  (q/stroke-weight 2)
  {:states (mapv make-state traces)
   :current 0 :start 0 :height 10})

(def event-color
  (memoize
   (let [color (atom 0)]
     (fn [event-key]
       (swap! color (comp #(mod % 256) (partial + 112)))
       [@color 150 256]))))

(def cog-name
  (memoize
   (let [id (atom 0)
         subscripts (zipmap [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9]
                            [\₀ \₁ \₂ \₃ \₄ \₅ \₆ \₇ \₈ \₉])]
     (fn [_] (apply str "Cog" (map subscripts (str (swap! id inc))))))))

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
      (q/text (cog-name cog) x y))))

(defn draw-time [trace history wd hd]
  (loop [i 0.0 t1 0 [x & xs] history]
    (when-let [t2 (event-key-time trace (first x))]
      (when (< t1 t2)
        (let [x1 (/ wd 2)
              x2 (- (q/width) x1)
              y (- (* (inc i) hd) (/ hd 2))]
          (q/with-stroke [0]
            (q/line x1 y x2 y))
          (q/text (str t2) (/ wd 4) y)))
      (recur (inc i) t2 xs))))

(defn draw-events [trace history cogs deadlocks wd hd]
  (doseq [[i events] (map-indexed vector history)]
    (if (= (event-key-type trace (first events)) :time)
      (draw-time trace events i wd hd)
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
          (when (deadlocks ((juxt :caller_id :local_id)
                            (event-key->event trace [cog id])))
            (q/stroke 0)
            (q/stroke-weight 3)
            (q/ellipse x y 17 17)
            (q/stroke-weight 2))
          (q/fill 0)
          (q/text method tx (- y (/ hd 4)))
          (q/text (name type) x (+ y (/ hd 4)))
          (q/stroke 0)
          (when-not (= method "init")
            (if-let [[cog2 id2] (first (enabled-by [cog id] trace))]
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
                  (dotted-arrow x y x2 y2))))))))))

(defn draw-state [{:keys [:states :current :start :height]}]
  (let [state (states current)
        history (take height (drop start (:history state)))
        n (count (:cogs state))
        m (count history)
        wd (/ (q/width) (inc n))
        hd (/ (q/height) (inc m))]
    (q/background 255)
    (draw-grid n m (/ wd 2) hd)
    (draw-cogs (:cogs state) wd hd)
    (draw-time (:trace state) history wd hd)

    (draw-events (:trace state) history (:cogs state) (:deadlocks state) wd hd)
    (when (> (count states) 1)
      (q/text (str (inc current) "/" (count states))
              (/ (q/width) 2) (- (q/height) (/ hd 4))))))
