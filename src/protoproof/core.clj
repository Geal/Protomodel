(ns protoproof.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(foo "aa")

(run* [q]
        (== q true))

(run* [q]
        (== q 1))

(db-rel user p)

(def users
  (db
    [user 'Alice]
    [user 'Bob]
    [user 'Eve]
  )
)

(db-rel generates u x)

(db-rel sendmsg a b x)

(defn knows [u x]
  (conde
    ((generates u x))
    ((fresh [a]
      (generates a x)
      (sendmsg a u x)
    ))
  )
)

(def knowledge
  (db
    [generates 'Alice 'abc]
    [generates 'Bob 'def2]
    [sendmsg 'Alice 'Bob 'abc]
  )
)




(defn tst [q]
  (all (user q) (knows q 'def2))
)


(with-dbs [users knowledge]
  ;(run* [q] (tst q))
  (run* [q] (all (user q) (knows q 'abc)))
)

