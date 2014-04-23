(ns protoproof.crypto-test
  (:refer-clojure :exclude [==])
  (:require [clojure.test :refer :all]
            [protoproof.crypto :refer :all]
  )
  (:use     [clojure.core.logic.pldb]
            [clojure.core.logic :exclude [is]]))

(db-rel generates u x)
(def knows
  (tabled [u x]
    (conde
      [(generates u x)]
      [(knowc u x)]
    )
  )
)

(def knowledge
  (db
    [generates 'Alice 'abc]
    [power 'g 'abc 'y]
  )
)

(deftest knowc-test
  (testing "knowc works"
    (is (= '(Alice)
            (with-dbs [knowledge]
              (run* [q] (all (knows q 'abc))))
        )
    )
  )
)

(def powers
  (db
      [power 'g 'a 'ga]
      [power 'g 'b 'gb]
      [power 'ga 'b 'gab]
  )
)


(deftest gpow-test
  (testing "exponentiation is commutative"
    (is (= '(b a)
            (with-dbs [powers]
              (run* [q] (all (fresh [x] (gpow x q 'gab))))
            )
           ) )
  )
)
