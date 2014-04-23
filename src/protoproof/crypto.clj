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

(db-rel asym-keys algorithm priv pub)
(db-rel asym-enc algorithm pub plaintext ciphertext)
(defn asym-dec [algorithm priv ciphertext plaintext]
  (fresh [pub]
    (asym-keys algorithm priv pub)
    (asym-enc algorithm pub plaintext ciphertext)
  )
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
    ; x is a ciphertext for which we know the public key
    [(fresh [alg priv pub plaintext]
      (knows u plaintext)
      (knows u pub)
      (asym-keys alg priv pub)
      (asym-enc alg pub plaintext x)
    )]
    [(fresh [alg priv pub ciphertext]
      (knows u ciphertext)
      (knows u priv)
      (asym-keys alg priv pub)
      (asym-dec alg priv ciphertext x)
    )]
  )
)

;(macroexpand '(knowc u x))
;(db-rel generates u x)
;(def asymmetric-encryption
;  (db
;    [generates 'Alice 'abc]
;    [generates 'Alice 'pub]
;    [asym-keys 'rsa 'priv 'pub]
;    [asym-enc 'rsa 'pub 'abc 'ciphertext]
;  )
;)

;(def knows
;  (tabled [u x]
;    (conde
;      [(generates u x)]
      ;  [(knowc u x)]
;    )
;  )
;)
;(with-dbs [asymmetric-encryption]
;  (run* [q] (all (knows q 'ciphertext)))
;)
;(with-dbs [asymmetric-encryption]
;  (run* [q] (all (fresh [pub] (asym-enc 'rsa pub 'abc q))))
;)
