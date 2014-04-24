(ns protoproof.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb])
  (:use [protoproof.crypto])
  (:use [protoproof.transport])
)

(db-rel user p)

(def users
  (db
    [user 'Alice]
    [user 'Bob]
    [user 'Eve]
    [user 'Mallory]
  )
)

(db-rel generates u x)

; "calculate" knowledge: data obtained from operations

;(def knows
;  (tabled [u x]
;    (conde
;      [(generates u x)]
;      [(know-transport-eavesdropper u x)]
;      [(knowc u x)]
;    )
;  )
;)

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

;(with-dbs [users knowledge]
;  (run* [q] (all (user q) (knows q 'abc)))
;)

;(with-dbs [users knowledge]
;  (run* [q] (all (fresh [x] (power 'g x 'y) (knows q x))))
;)

;(with-dbs [users knowledge]
;  (run* [q] (all (knows q 'y)))
;)

