;; We import all the necessary unity items and arcadia functions
(ns minimal.benchmark
  (:import [UnityEngine Input KeyCode Camera Physics Time Resources GameObject Vector2 Vector3 Transform Quaternion Rigidbody2D Physics2D Mathf])
  (:use arcadia.core arcadia.linear))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Setup & Management functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn boi [] (object-named "Main Camera"))

;; https://github.com/arcadia-unity/Arcadia/blob/6e7dda6775725407a7a180b3afbb25a9742c6977/Source/arcadia/internal/filewatcher.clj
(defn- before? [^DateTime d1 ^DateTime d2]
  (< (DateTime/Compare d1 d2) 0))

(defn play-timer []
  (let [tar (object-named "Main Camera")]
    (state+ tar :timerStart (DateTime/UtcNow))))

(defn check-timer []
  (let [tar (object-named "Main Camera")]
    (* 100 (.Ticks (.Subtract  DateTime/UtcNow (state tar :timerStart))))))

(defn reset-mark-values []
  (let [tar (object-named "Main Camera")]
    (do
      (state+ tar :sCount 1)
      (state+ tar :totalCount 0)
      (state+ tar :dummy 0.0)
      (state+ tar :runningTime 0.0)
      (state+ tar :timerStart (DateTime/UtcNow))
      (state+ tar :deltaTime 0.0)
      (state+ tar :deltaTimeSquared 0.0))))

(defn stateAddUpdate [value varName]
  (let [tar (object-named "Main Camera")]
    (state+ tar varName (+ (state tar varName) value))))

(defn stateUpdate [value varName]
  (let [tar (object-named "Main Camera")]
    (state+ tar varName value)))

(defn stateGet [varName]
  (let [tar (object-named "Main Camera")]
    (state tar varName)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Mark 8 function
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn mark8 [msg, fun, iterations, mintime]
  (let [tar (object-named "Main Camera")]
    (do
      (reset-mark-values)
      (while (and (< (state tar :runningTime) mintime) ;;Begin do/while loop for test
                  (< (state tar :sCount) (/ 2147483647 2)))
        (do
          (state+ tar :sCount (* 2 (state tar :sCount)))
          (state+ tar :deltaTime 0.0)
          (state+ tar :deltaTimeSquared 0.0)
          (dotimes [j iterations]
            (do
              (play-timer)
              (dotimes [i (state tar :sCount)]
                (do
                  (stateAddUpdate (fun i) :dummy)
                  )
                )
              (stateUpdate (check-timer) :runningTime)

              (let [ttime (/ (state tar :runningTime) (state tar :sCount))]
                (do
                  (stateAddUpdate ttime :deltaTime)
                  (stateAddUpdate (* ttime ttime) :deltaTimeSquared)
                  (stateAddUpdate (state tar :sCount) :totalCount)
                  (let [mean (/ (state tar :deltaTime) iterations)]
                    (let [stddev (Mathf/Sqrt (/  (- (state tar :deltaTimeSquared) (* mean (* iterations mean))) (- iterations 1) ))]
                      (log msg mean stddev (state tar :sCount)))
                    )

                  )
                ))))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Benchmark tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; For testing only
(defn funtest [i]
  (* i i))

;; 2D
(defn scale2d [i]
  (.x (v2* (v2) i)))

(defn multiply2d [i]
  (.x (v2scale (v2) (v2 i i))))

(defn translate2d [i]
  (.x (v2+ (v2) (v2 i i))))

(defn subtract2d [i]
  (.x (v2- (v2 i i) (v2))))

(defn length2d [i]
  (.magnitude (v2 i i)))

(defn dot2d [i]
  (UnityEngine.Vector2/Dot (v2) (v2 i i)))

;; 3D
(defn scale3d [i]
  (.x (v3* (v3) i)))

(defn multiply3d [i]
  (.x (v3scale (v3) (v3 i i i))))

(defn translate3d [i]
  (.x (v3+ (v3) (v3 i i i))))

(defn subtract3d [i]
  (.x (v3- (v3 i i i) (v3))))

(defn length3d [i]
  (.magnitude (v3 i i i)))

(defn dot3d [i]
  (UnityEngine.Vector3/Dot (v3) (v3 i i i)))

;; Math

(defn sestoft [i]
  (let [d (* 1.1 (bit-and i 0xFF))]
    (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d (* d d)))))))))))))))))))))

(defn sestoftpow [i]
  (let [d (* 1.1 (bit-and i 0xFF))]
    (Mathf/Pow d 20)))

(defn primes [i]
  (let [realNumber 100
        tar (object-named "Main Camera")]
    (do
      (stateUpdate :A (boolean-array (+ realNumber 1)))
      (log (stateGet :A))
      (log (stateGet :runningTime))
      )
    ))

(defn memtest [i]
  (bit-and (alength (int-array 100000)) i))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entry point for testing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn run-tests-on-press [o,id]
  (when (Input/GetKeyDown KeyCode/Space)
    (do
      (log "Msg\tMean\tDev\tCount")
      (mark8 "hi" funtest 5 0.250)
      (mark8 "Scale2d" scale2d 5 0.250)
      (mark8 "Scale3d" scale3d 5 0.250)
      (mark8 "Multiply2d" multiply2d 5 0.250)
      (mark8 "Multiply3d" multiply3d 5 0.250)
      (mark8 "Translate2d" translate2d 5 0.250)
      (mark8 "Translate3d" translate3d 5 0.250)
      (mark8 "Subtract2d" subtract2d 5 0.250)
      (mark8 "Subtract3d" subtract3d 5 0.250)
      (mark8 "Length2d" length2d 5 0.250)
      (mark8 "Length3d" length3d 5 0.250)
      (mark8 "Dot2d" dot2d 5 0.250)
      (mark8 "Dot3d" dot3d 5 0.250)
      (mark8 "Sestoft" sestoft 5 0.250)
      (mark8 "SestoftPow" sestoftpow 5 0.250)
      (mark8 "Memtest" memtest 5 0.250)
      (primes 5)
      (log "Done running Test"))))
