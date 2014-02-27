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
(db-rel user p)
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

(def knowledge
  (db
    [generates 'Alice 'abc]
    [generates 'Bob 'def]
  )
)


(db-rel sendmsg a b x)

(defn knows [u x]
  (conde
    (generates u x)
    ((fresh [a]
      (generates a x)
      (sendmsg a u x)
    ))
  )
)
(with-dbs [users knowledge]
  (run* [q] (user q) (knows q 'abc))
)

