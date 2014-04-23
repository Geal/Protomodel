(ns protoproof.transport
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb])
)

(db-rel transport tr)
(db-rel listener tr u)
(db-rel sender tr u)
(db-rel sendmsg tr a b x)

(defn eavesdrop [tr e x]
  (fresh [a b]
    (listener tr e)
    (sendmsg tr a b x)
  )
)

(defn recv-basic [transport bob message]
  (fresh [alice]
    (sendmsg transport alice bob message)
  )
)

(defmacro know-transport-basic [u x]
  '(fresh [a tr]
    (transport tr)
    (generates a x)
    (recv-basic tr u x)
  )
)

(defmacro know-transport-eavesdropper [u x]
  '(fresh [a tr]
    (transport tr)
    (generates a x)
    (conde
      ((recv-basic tr u x))
      ((eavesdrop tr u x))
    )
  )
)
