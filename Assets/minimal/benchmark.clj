;; We import all the necessary unity items and arcadia functions
(ns minimal.benchmark
  (:import [System.IO File]
           [UnityEngine Input KeyCode Camera Physics Time Resources GameObject Vector2 Vector3 Transform Quaternion Rigidbody2D Physics2D Mathf])
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

(defn resultOutput [msg mean std sCount]
  (File/AppendAllText "out.txt" (format "%s,%f,%f,%d\n" msg (float mean) (float std) sCount)))

(def createOutput
    (File/WriteAllText "out.txt" "Msg, Mean, Deviation, Count\n"))

(def testDuration
  0.250)


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
                      (resultOutput msg mean stddev (stateGet :sCount))
                      ;;(log "test")
                      )
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

(defn prime-helper [realN i]
  (let [iPow (Mathf/Floor (Mathf/Pow i 2))]
    (loop [j 0
           nu 0]
      (when (< j realN)
        (stateUpdate (into-array (assoc (vec (stateGet :A)) i false)) :A)
        (recur (+ iPow (* i (+ nu 1))) (inc nu))
        ))
    ))

(defn primes [number]
  (let [realNumber 100
        tar (object-named "Main Camera")]
    (do
      (stateUpdate (boolean-array (+ realNumber 1) true) :A)
      (loop [i 2]
        (when (< i (Mathf/Sqrt realNumber))
          (when (nth (stateGet :A) i)
            (prime-helper realNumber i))))
      (loop [inew 2
             primes []]
        (if (< inew (alength (stateGet :A)))
          (if (nth (stateGet :A) inew)
            (recur (inc inew) (conj primes inew))
            (recur (inc inew) primes))
          (bit-and (last primes) number))))))


;;  (for [x (range 0 101) :while (< x (Mathf/Sqrt 100)) :let [y (+ 5 x)]] y)

(defn memtest [i]
  (bit-and (alength (int-array 100000)) i))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entry point for testing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn run-tests-on-press [o,id]
  (when (Input/GetKeyDown KeyCode/Space)
    (do
      (log "Starting tests..")
      (mark8 "Scale2d" scale2d 5 testDuration)
      (mark8 "Scale3d" scale3d 5 testDuration)
      (mark8 "Multiply2d" multiply2d 5 testDuration)
      (mark8 "Multiply3d" multiply3d 5 testDuration)
      (mark8 "Translate2d" translate2d 5 testDuration)
      (mark8 "Translate3d" translate3d 5 testDuration)
      (mark8 "Subtract2d" subtract2d 5 testDuration)
      (mark8 "Subtract3d" subtract3d 5 testDuration)
      (mark8 "Length2d" length2d 5 testDuration)
      (mark8 "Length3d" length3d 5 testDuration)
      (mark8 "Dot2d" dot2d 5 testDuration)
      (mark8 "Dot3d" dot3d 5 testDuration)
      (mark8 "Sestoft" sestoft 5 testDuration)
      (mark8 "SestoftPow" sestoftpow 5 testDuration)
      (mark8 "Primes" primes 5 testDuration)
      (mark8 "Memtest" memtest 5 testDuration)
      (log "Done running Tests"))))
