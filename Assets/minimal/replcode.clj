;; strings work
(log "Hello, Arcadia!")

;; Reload everything
(do
  (require '[arcadia.core :reload :all])
  (require '[arcadia.linear :reload :all])
  (require '[minimal.mmo  :reload :all])
  (require '[minimal.core :reload :all])
  (require '[minimal.benchmark :reload :all]))

;; Load everything
(do
  (require '[arcadia.core :refer :all])
  (require '[arcadia.linear :refer :all])
  (require '[minimal.mmo  :refer :all])
  (require '[minimal.core :refer :all])
  (require '[minimal.benchmark :refer :all]))

(require '[ :refer :all])
(require '[ :reload :all])


(objects-named "object-1")


(hook+ (first (objects-named "object-1")) :update #'minimal.core/first-callback)
(hook+ (get-player) :update #'minimal.core/first-callback)

(roles+ (object-named "Projectile")
        (-> projectile-roles
            (assoc-in [::lifespan :state :start] System.DateTime/Now)
            (assoc-in [::lifespan :state :lifespan] 5000)))
