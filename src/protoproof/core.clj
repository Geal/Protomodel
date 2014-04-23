(ns protoproof.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb])
  (:require [clojure.test]))

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

(db-rel gpow gx x)

; "calculate" knowledge: data obtained from operations
(defn knowc [u x]
  (conde
    ((generates u x))
    ((fresh [w]
      (generates u w)
      (gpow x w)
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
      [(fresh [w]
        (knows u w)
        (gpow x w)
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
    [gpow 'y 'abc]
  )
)

(defn tst [q]
  (all (user q) (knows q 'def2))
)

(with-dbs [users knowledge]
  (run* [q] (all (user q) (knows q 'abc)))
)

(with-dbs [users knowledge]
  (run* [q] (all (fresh [x] (gpow x 'abc) (knowc q x))))
)

; test x is a g^a
(with-dbs [users knowledge]
  (run* [q] (all (knows q 'y)))
)

(clojure.test/deftest test-adder
  (clojure.test/is (= ('Alice 'Bob 'Eve)  (
    with-dbs [users knowledge]
      (run* [q] (all (knows q 'y)))
                           ))
  ))
;(with-dbs [users knowledge]
;  (run* [q] (all (user q) (knows q 'abc)))
;)
(clojure.test/run-tests)
