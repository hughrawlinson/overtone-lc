(ns my-symphony.song
  (:require [overtone.live :refer :all]
            ; [overtone.inst.drum :as drum]
            [leipzig.melody :refer :all]
            [leipzig.scale :as scale]
            [leipzig.live :as live]
            [leipzig.chord :as chord]
            [leipzig.temperament :as temperament]))
(->>
  (phrase [3/3   3/3   2/3   1/3   3/3]
         [  0     0     0     1     2])
  (where :pitch (comp scale/lower))
  (all :part :kick))

; Instruments
(definst bass [freq 110 volume 1.0]
  (-> (saw freq)
      (* (env-gen (perc 0.1 0.4) :action FREE))
      (* volume)))

(definst organ [freq 440 dur 1 volume 1.0]
  (-> (square freq)
      (* (env-gen (adsr 0.01 0.8 0.1) (line:kr 1 0 dur) :action FREE))
      (* 1/4 volume)))

(definst kick [amp 0.7 decay 0.6 freq 65]
  (* (sin-osc freq (* Math/PI 0.5))
     (env-gen (perc 0 decay) 1 1 0 1 FREE)
     amp))

(def kick (sample (freesound-path 171104)))
(kick)

; (drum/kick)

; Arrangement
(defmethod live/play-note :bass [{hertz :pitch}] (bass hertz))
(defmethod live/play-note :kick [{hertz :pitch}] (kick))
(defmethod live/play-note :accompaniment [{hertz :pitch seconds :duration}] (organ hertz seconds))


(def claprhythm [1 1 3/4 3/4 2/4])

; Composition
(def progression [5 3 0 4])

(defn kickline [root]
  (->> (phrase claprhythm [0 -3 -1 0 2])
       (where :pitch (scale/from root))
       (where :pitch (comp scale/lower scale/lower))
       (all :part :kick)))

(defn bassline [root]
  (->> (phrase [1.5 1.5 1] [0 4 0])
       (where :pitch (scale/from root))
       (where :pitch (comp scale/lower scale/lower scale/lower))
       (all :part :bass)))

(defn accompaniment [root]
  (->>
    (phrase [4] [(-> chord/triad (chord/root root))])
    (where :pitch (sharp))
    (all :part :accompaniment)))

; Track
(def track
  (->>
    (mapthen kickline progression)
    (with (mapthen accompaniment progression))
    (with (mapthen bassline progression))
    (where :pitch (comp temperament/equal scale/B scale/major))
    (tempo (bpm 114.944))))

(defn -main []
  (live/play track))

(comment
  ; Loop the track, allowing live editing.
  (live/jam (var track))
  (live/stop)
  )
