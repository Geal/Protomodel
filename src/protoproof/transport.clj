(ns protoproof.transport
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb])
)

(db-rel transport tr)
(db-rel listener tr u)
(db-rel sender tr u)
(db-rel intercepter tr u)
(db-rel sendmsg tr a b x)

(defn eavesdrop [tr e x]
  (fresh [a b]
    (listener tr e)
    (sendmsg tr a b x)
  )
)

(defn basic-recv [transport bob message]
  (fresh [alice]
    (sendmsg transport alice bob message)
  )
)

(defmacro know-transport-basic [u x]
  '(fresh [a tr]
    (transport tr)
    (generates a x)
    (basic-recv tr u x)
  )
)

(defmacro know-transport-eavesdropper [u x]
  '(fresh [a tr]
    (transport tr)
    (generates a x)
    (conde
      ((basic-recv tr u x))
      ((eavesdrop tr u x))
    )
  )
)
