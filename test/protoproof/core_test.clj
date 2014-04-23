(ns protoproof.core-test
  (:refer-clojure :exclude [==])
  (:require [clojure.test :refer :all]
            [protoproof.core :refer :all]
            [protoproof.crypto :refer :all]
  )
  (:use     [clojure.core.logic.pldb]
            [clojure.core.logic :exclude [is]]))


(def usersdb
  (db
    [user 'Alice]
    [user 'Bob]
    [user 'Eve]
  )
)

(def protocol
  (db
    [transport 'tr]
    [generates 'Alice 'abc]
    [generates 'Bob 'def2]
    [listener 'tr 'Eve]
    [sendmsg 'tr 'Alice 'Bob 'abc]
    [power 'g 'abc 'y]
  )
)

(deftest eavesdrop-test
  (testing "Eve can eavesdrop"
    (is (= '(Alice Bob Eve) (
      with-dbs [users knowledge]
        (run* [q] (all (knows q 'y)))
      ))
    )
  )
)

