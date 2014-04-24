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
      [(know-transport-mitm u x)]
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
    [intercepter 'tr 'Mallory]
    [generates 'Alice 'a]
    [generates 'Alice 'g]
    [generates 'Bob 'g]
    [generates 'Mallory 'g]
    [generates 'Mallory 'm]
    [power 'g 'a 'ga]
    [power 'g 'm 'gm]
    [sendmsg 'tr 'Alice 'Bob 'ga]
    [mitm 'tr 'Mallory 'ga 'gm]
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

; what does Mallory know?
(with-dbs [usersdb step1]
  (run* [q] (all (knows 'Mallory q)))
)
; => (g m gm ga)

(def step2
  (db
    [generates 'Bob 'b]
    [power 'g 'b 'gb]
    [power 'gm 'b 'gmb]
    [power 'ga 'm 'gam]
    ;[power 'ga 'b 'gab]
    ;[power 'gb 'a 'gab]
    [sendmsg 'tr 'Bob 'Alice 'gb]
    [mitm 'tr 'Mallory 'gb 'gm]
  )
)

; what does Alice know?
(with-dbs [usersdb step1 step2]
  (run* [q] (all (knows 'Alice q)))
)
; => (a g ga gm gam)

; what does Bob know?
(with-dbs [usersdb step1 step2]
  (run* [q] (all (knows 'Bob q)))
)
; => (g b gb gm gmb)

; what does Mallory know?
(with-dbs [usersdb step1 step2]
  (run* [q] (all (knows 'Mallory q)))
)
; => (g m gm gb ga gmb gam)

(def step3
  (db
    ;[power 'gb 'a 'gba]
    [generates 'Alice 'hello]
    [sym-enc 'aes 'gam 'hello 'encryptedmsg]
    [sendmsg 'tr 'Alice 'Bob 'encryptedmsg]
    [sym-enc 'aes 'gmb 'hello 'reencryptedmsg]
    [mitm 'tr 'Mallory 'encryptedmsg 'reencryptedmsg]
  )
)

; what does Alice know?
(sort (with-dbs [usersdb step1 step2 step3]
  (run* [q] (all (knows 'Alice q)))
))
; => (a encryptedmsg g ga gam gm hello)

; what does Bob know?
(sort (with-dbs [usersdb step1 step2 step3]
  (run* [q] (all (knows 'Bob q)))
))
; => (b g gb gm gmb hello reencryptedmsg)

; what does Eve know?
(sort (with-dbs [usersdb step1 step2 step3]
  (run* [q] (all (knows 'Mallory q)))
))
; => encryptedmsg g ga gam gb gm gmb hello m reencryptedmsg)
