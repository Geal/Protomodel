(ns protoproof.crypto
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb])
)


(db-rel power g x gx)
(def gpow
  (tabled [g x gx]
    (conde
      [(power g x gx)]
      [(fresh [h, y, hx, hy]
        (gpow h x hx)
        (gpow h y g)
        (gpow hx y gx)
      )]
    )
  )
)

(db-rel hash-alg x hx)

(defmacro knowc [u x]
  '(conde
    [(fresh [g w]
      (knows u w)
      (gpow g w x)
    )]
  )
)

(macroexpand '(knowc u x))
