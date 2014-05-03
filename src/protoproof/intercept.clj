(ns protoproof.intercept
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb])
  (:use [protoproof.transport])
)

(db-rel intercepter tr u)
(db-rel dropm transport mallory message)
(db-rel mitm transport mallory message replaced)

(defn dropped [message]
  (fresh [transport alice bob mallory]
    (sendmsg transport alice bob message)
    (dropm transport mallory message)
  )
)

(defn replaced [message]
  (fresh [transport alice bob mallory replacedmessage]
    (sendmsg transport alice bob message)
    (mitm transport mallory message replacedmessage)
  )
)

(defn recv-mitm [transport bob message]
  (conde
    ; the message was not dropped or replaced
    [(fresh [alice]
      (sendmsg transport alice bob message)
      (nafc dropped message)
      (nafc replaced message)
    )]
    ; this is a replaced message
    [(fresh [alice mallory original]
      (sendmsg transport alice bob original)
      (mitm transport mallory original message)
    )]
  )
)

(defn eavesdrop-mitm [tr m x]
  (fresh [a b]
    (intercepter tr m)
    (sendmsg tr a b x)
  )
)

(defmacro know-transport-mitm [u x]
  '(fresh [a tr]
    (transport tr)
    ;(generates a x)
    (conde
      ; the message was not intercepted or dropped
      [
        (sendmsg tr a u x)
        (nafc dropped x)
        (nafc replaced x)
      ]
      ; the message was intercepted
      [(fresh [m original]
        (knows m x)
        (sendmsg tr a u original)
        (mitm tr m original x)
      )]
      ; u is an eavesdropper
      [(fresh [b]
        (knows a x)
        (listener tr u)
        (sendmsg tr a b x)
      )]
      ; u is an intercepter
      [(fresh [b]
        (knows a x)
        (intercepter tr u)
        (sendmsg tr a b x)
      )]
    )
  )
)
