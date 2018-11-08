(ns visualize-traces-clj.draw
  (:require [quil.core :as q :include-macros true]
            [visualize-traces-clj.dpor :refer [enabled-by trace->history]]
            [visualize-traces-clj.event-keys :refer :all]
            [visualize-traces-clj.utils :refer :all]))

(defn setup [trace]
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (q/text-align :center :center)
  (q/text-font (q/create-font "monospace" 16))
  (q/stroke-weight 2)
  {:trace trace
   :cogs (keys trace)
   :history (trace->history trace)
   :start 0
   :height 30})

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
  (q/text-size 14)
  (doseq [[i events] (map-indexed vector history)]
    (let [y (* (inc i) hd)]
      (doseq [[cog id] events]
        (let [type (event-key-type trace [cog id])
              task (event-key-task trace [cog id])
              method (event-key-method-name trace [cog id])
              x (* wd (inc (.indexOf cogs cog)))]
          (apply q/fill (event-color task))
          (q/no-stroke)
          (q/ellipse x y 15 15)
          (q/fill 0)
          (q/text method x (- y (/ hd 4)))
          (q/text (name type) x (+ y (/ hd 4)))
          (q/stroke 0)
          (doseq [[cog2 id2] (enabled-by [cog id] trace)]
            (let [x2 (* wd (inc (.indexOf cogs cog2)))
                  j  (count (take-while (complement #(% [cog2 id2])) history))
                  y2 (* (inc j) hd)
                  method (event-key-method-name trace [cog id])]
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
    (draw-events (:trace state) history (:cogs state) wd hd)))

(defn key-handler [state event]
  (case (name (q/key-as-keyword))
    " "    (update state :paused not)

    "up"   (update state :speed inc)

    "down" (update state :speed dec)

    state))

