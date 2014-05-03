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

(defn recv-basic [tr bob message]
  (fresh [alice]
    (sendmsg tr alice bob message)
  )
)

(defmacro know-transport-basic [knows u x]
  `(fresh [a# tr#]
    (transport tr#)
    (~knows a# ~x)
    (sendmsg tr# a# ~u ~x)
  )
)

(defmacro know-transport-eavesdropper [knows u x]
  `(fresh [a# b# tr#]
    (transport tr#)
    (conde
      [
       (~knows a# ~x)
       (sendmsg tr# a# ~u ~x)
      ]
      [
       (~knows a# ~x)
       (listener tr# ~u)
       (sendmsg tr# a# b# ~x)
      ]
    )
  )
)
