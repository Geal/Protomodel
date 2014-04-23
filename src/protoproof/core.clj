(ns protoproof.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb])
)

(db-rel user p)

(def users
  (db
    [user 'Alice]
    [user 'Bob]
    [user 'Eve]
  )
)

(db-rel generates u x)

(db-rel transport tr)
(db-rel listener tr u)
(db-rel sender tr u)

(db-rel sendmsg tr a b x)

;(defn recv [tr u x]
;  (fresh[a]
;    (knows a x)
;    (sendmsg tr a u x)
;  )
;)

(defn eavesdrop [tr e x]
  (fresh [a b]
    (listener tr e)
    (sendmsg tr a b x)
  )
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

; "calculate" knowledge: data obtained from operations
(defn knowc [u x]
  (conde
    ((generates u x))
    ((fresh [g w]
      (generates u w)
      (gpow g w x)
    ))
  )
)

(def knows
  (tabled [u x]
    (conde
      [(generates u x)]
      [(fresh [a tr]
        (transport tr)
        (generates a x)
        (conde
          ((sendmsg tr a u x))
          (( eavesdrop tr u x))
        )
      )]
      [(fresh [g w]
        (knows u w)
        (gpow g w x)
      )]
    )
  )
)

(def knowledge
  (db
    [transport 'tr]
    [generates 'Alice 'abc]
    [generates 'Bob 'def2]
    [listener 'tr 'Eve]
    [sendmsg 'tr 'Alice 'Bob 'abc]
    [power 'g 'abc 'y]
  )
)

(with-dbs [users knowledge]
  (run* [q] (all (user q) (knows q 'abc)))
)

(with-dbs [users knowledge]
  (run* [q] (all (fresh [x] (power 'g x 'y) (knows q x))))
)

(with-dbs [users knowledge]
  (run* [q] (all (knows q 'y)))
)

