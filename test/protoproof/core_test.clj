(ns protoproof.core-test
  (:refer-clojure :exclude [==])
  (:require [clojure.test :refer :all]
            [protoproof.core :refer :all]
            [protoproof.crypto :refer :all]
            [protoproof.transport :refer :all]
            [protoproof.intercept :refer :all]
  )
  (:use     [clojure.core.logic.pldb]
            [clojure.core.logic :exclude [is]]))


(def usersdb
  (db
    [user 'Alice]
    [user 'Bob]
    [user 'Eve]
    [user 'Mallory]
  )
)

(def protocol
  (db
    [transport 'tr]
    [generates 'Alice 'g]
    [generates 'Bob 'g]
    [generates 'Eve 'g]
    [generates 'Alice 'abc]
    [generates 'Bob 'def2]
    [listener 'tr 'Eve]
    [sendmsg 'tr 'Alice 'Bob 'abc]
    [power 'g 'abc 'y]
  )
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

(deftest eavesdrop-test
  (testing "Eve can eavesdrop"
    (is (= '(Alice Bob Eve) (sort (
      with-dbs [users protocol]
        (run* [q] (all (knows q 'y)))
      )))
    )
  )
)

