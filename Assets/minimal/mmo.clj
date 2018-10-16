(ns minimal.mmo
  (:import [UnityEngine Input KeyCode Camera Physics Time Resources GameObject Vector2 Vector3 Transform Quaternion Rigidbody2D Physics2D Mathf])
  (:use arcadia.core arcadia.linear))


;;(defn shoot [^Vector2 position]
;;  (-> (Resources/Load "Projectile")
;;      (instantiate position)))


;;Associate a lifespan to a object, on update, check if timepassed > lifespan
(defrole lifespan-role
  :state { :start System.DateTime/Now
          :lifespan 0}
  (update [obj k]
          (let [{:keys [start lifespan]} (state obj k)]
            (when (< lifespan (.TotalMilliseconds (.Subtract System.DateTime/Now start)))
              (retire obj)))))

(def projectile-roles
  {::lifespan lifespan-role})

(defn get-player []
  (object-tagged "Player"))

(defn get-paddles []
  (objects-tagged "Paddle"))


(defn say-name [o]
  (doseq [tar o] (log "Hi, i'm" tar)))

(defn shoot []
  (let [proj (instantiate
               (Resources/Load "Projectile"))] 
    (with-cmpt proj [tr Transform] 
      (set! (.position tr) (.position (cmpt (get-player) UnityEngine.Transform)))
      (roles+ proj
              (-> projectile-roles
                  (assoc-in [::lifespan :state :start] System.DateTime/Now)
                  (assoc-in [::lifespan :state :lifespan] 5000)))
      proj)))

(defn move-when-arrow-pressed [o, id]
  (let [yMax 6.5 yMin -2.33 speed 10]
    (when (Input/GetKey KeyCode/W)
      (with-cmpt o [trans Transform]
        (.Translate trans (v3 0 (* speed Time/deltaTime) 0))))
    (when (Input/GetKey KeyCode/S)
      (with-cmpt o [trans Transform]
        (.Translate trans (v3 0 (-(* speed Time/deltaTime)) 0))))
    (when (Input/GetKeyDown KeyCode/Space)
      (shoot))))

;;(def obstacle-role
;;  :update )

;;(def player-role
;;  {:update '#move-when-arrow-pressed})

(defn loggerboi []
  (log "OnStartpls"))

(defn move-obstacle [obs k]
  (with-cmpt obs [rb Rigidbody2D]
    (.AddForce rb (v2 -10 0))))
