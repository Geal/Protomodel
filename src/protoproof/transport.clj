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
