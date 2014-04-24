(ns protoproof.examples.dh
  (:refer-clojure :exclude [==])
  (:use     [clojure.core.logic.pldb]
            [clojure.core.logic :exclude [is]])
  (:require [protoproof.core :refer :all]
            [protoproof.crypto :refer :all]
            [protoproof.transport :refer :all]
            [protoproof.intercept :refer :all])
)

(def knows
  (tabled [u x]
    (conde
      [(generates u x)]
      [(know-transport-eavesdropper u x)]
      [(knowc u x)]
    )
  )
)

(def usersdb
  (db
    [user 'Alice]
    [user 'Bob]
    [user 'Eve]
    [user 'Mallory]
  )
)

(def step1
  (db
    [transport 'tr]
    [listener 'tr 'Eve]
    [generates 'Alice 'a]
    [generates 'Alice 'g]
    [generates 'Bob 'g]
    [generates 'Eve 'g]
    [power 'g 'a 'ga]
    [sendmsg 'tr 'Alice 'Bob 'ga]
  )
)

; what does Alice know?
(with-dbs [usersdb step1]
  (run* [q] (all (knows 'Alice q)))
)
; => (a g ga)

; what does Bob know?
(with-dbs [usersdb step1]
  (run* [q] (all (knows 'Bob q)))
)
; => (g ga)

; what does Eve know?
(with-dbs [usersdb step1]
  (run* [q] (all (knows 'Eve q)))
)
; => (g ga)

(def step2
  (db
    [generates 'Bob 'b]
    [power 'g 'b 'gb]
    [power 'ga 'b 'gab]
    ;[power 'gb 'a 'gab]
    [sendmsg 'tr 'Bob 'Alice 'gb]
  )
)

; what does Alice know?
(with-dbs [usersdb step1 step2]
  (run* [q] (all (knows 'Alice q)))
)
; => (a g ga gb gab)

; what does Bob know?
(with-dbs [usersdb step1 step2]
  (run* [q] (all (knows 'Bob q)))
)
; => (g b gb ga gab)

; what does Eve know?
(with-dbs [usersdb step1 step2]
  (run* [q] (all (knows 'Eve q)))
)
; => (g gb ga)

(def step3
  (db
    [power 'gb 'a 'gba]
    [generates 'Alice 'hello]
    [sym-enc 'aes 'gba 'hello 'encryptedmsg]
    [sendmsg 'tr 'Alice 'Bob 'encryptedmsg]
    ;[sym-dec 'aes 'gba 'encryptedmsg 'plaintext]
  )
)

(with-dbs [usersdb step1 step2 step3]
  (run* [q] (all
    (fresh [ciphertext k]
      (knows 'Bob ciphertext)
      (knows 'Bob k)
      ;(knows 'Bob q)
      (sym-dec 'aes 'gba ciphertext q)
    )
  ))
)

; what does Alice know?
(sort (with-dbs [usersdb step1 step2 step3]
  (run* [q] (all (knows 'Alice q)))
))
; => (a encryptedmsg g ga gab gb gba hello)

; what does Bob know?
(sort (with-dbs [usersdb step1 step2 step3]
  (run* [q] (all (knows 'Bob q)))
))
; => (b encryptedmsg g ga gab gb gba hello)

; what does Eve know?
(sort (with-dbs [usersdb step1 step2 step3]
  (run* [q] (all (knows 'Eve q)))
))
; => (encryptedmsg g ga gb)
