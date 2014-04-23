(ns protoproof.crypto
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb])
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

(db-rel hashing alg x hx)

(db-rel sym-enc algorithm k plaintext ciphertext)
(defn sym-dec [algorithm k ciphertext plaintext]
  (sym-enc algorithm k plaintext ciphertext)
)

(defmacro knowc [u x]
  '(conde
    ; g is a known power
    [(fresh [g w]
      (knows u w)
      (gpow g w x)
    )]
    ; we know a preimage
    [(fresh [alg prev]
      (knows u prev)
      (hashing alg prev x)
    )]
    ; x is a ciphertext for which we know the key
    [(fresh [alg prev k]
      (knows u prev)
      (knows u k)
      (sym-enc alg k prev x)
    )]
    [(fresh [alg ciphertext k]
      (knows u ciphertext)
      (knows u k)
      (sym-dec alg k ciphertext x)
    )]
  )
)

(macroexpand '(knowc u x))
