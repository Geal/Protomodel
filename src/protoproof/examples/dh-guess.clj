(ns protoproof.examples.dh
  (:refer-clojure :exclude [==])
  (:use     [clojure.core.logic.pldb]
            [clojure.core.logic :exclude [is]]
            [clojure.test])
  (:require [protoproof.core :refer :all]
            [protoproof.crypto :refer :all]
            [protoproof.transport :refer :all]
            [protoproof.intercept :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop])
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
    [sendmsg 'tr 'Alice 'Bob 'a]
    ;[pass 'tr 'Mallory 'a]
    ;[generates 'Alice 'g]
    ;[generates 'Bob 'g]
    ;[generates 'Mallory 'g]
    ;[generates 'Mallory 'm]
    ;[power 'g 'a 'ga]
    ;[power 'g 'm 'gm]
    ;[sendmsg 'tr 'Alice 'Bob 'ga]
    ;[mitm 'tr 'Mallory 'ga 'gm]
  )
)

(with-dbs [usersdb step1]
  (run* [q] (all (knows 'Mallory q) ))
)

(def usersgen (gen/elements ['Alice 'Bob 'Mallory]))
(def datagen (gen/elements ['a 'x 'y]))
;(def datagenexcepta (gen/elements ['x 'y]))
(def datagenexcepta (gen/elements ['x]))
(def sentmsggen (gen/elements
  (with-dbs [usersdb step1]
    (run* [q] (all (fresh [a b] (sendmsg 'tr a b q)) ))
  )
))

(gen/sample sentmsggen)
(gen/sample usersgen)
(gen/sample datagen)

(def relgen
  (gen/one-of
    [
     ;(gen/fmap (fn [[u data]] [generates u data]) (gen/tuple usersgen datagen))
     ;(gen/fmap (fn [[a b data]] [sendmsg 'tr a b data]) (gen/tuple usersgen usersgen datagen))
     (gen/fmap (fn [[data]] [generates 'Mallory data]) (gen/tuple datagenexcepta))
     (gen/fmap (fn [[data]] [pass 'tr 'Mallory data]) (gen/tuple sentmsggen))
     (gen/fmap (fn [[data1 data2]] [mitm 'tr 'Mallory data1 data2]) (gen/tuple sentmsggen datagenexcepta))
    ]
  )
)

(gen/sample (gen/vector relgen))

(def samples (last (gen/sample (gen/vector relgen))))
(println samples)
(with-dbs [usersdb step1 (apply db samples)]
  (run* [q] (all (knows 'Bob q) ))
)
(with-dbs [usersdb step1 (apply db samples)]
  (run* [q] (all (knows 'Mallory q) ))
)
(with-dbs [usersdb step1 (apply db samples)]
  (run* [q] (all (knows 'Alice q) ))
)

(def test-samples-db
  (prop/for-all [samples (gen/vector relgen)]
    (= '()
      (with-dbs [usersdb step1 (apply db samples)]
        (run* [q] (all (knows 'Bob q) ))
      )
    )
))

(tc/quick-check 100 test-samples-db)
;(def samplesdb
;  (apply db samples)
;)
;(println samplesdb)
;(with-dbs [usersdb step1 samplesdb]
;  (run* [q] (all (knows 'Mallory q) ))
;)
;(run-tests)

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

