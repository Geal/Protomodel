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
      with-dbs [users protocol]
        (run* [q] (all (knows q 'y)))
      ))
    )
  )
)

(def knows-mitm
  (tabled [u x]
    (conde
      [(generates u x)]
      [(know-transport-mitm u x)]
      [(knowc u x)]
    )
  )
)

(def dropmessage
  (db
    ; intercepter does not let the message pass, it is dropped
    [intercepter 'tr 'Mallory]
  )
)

(deftest dropmessage-test
  (testing "Mallory can drop message"
    (is (= '() (
      with-dbs [users protocol dropmessage]
        (run* [q] (all (recv-mitm 'tr q 'abc)))
      ))
    )
  )
)

(def replacemessage
  (db
    [intercepter 'tr 'Mallory]
    [mitm 'tr 'Mallory 'abc 'hello]
  )
)

(deftest dropmessage-test
  (testing "Mallory can replace message"
    (is (= '(Bob) (
      with-dbs [users protocol replacemessage]
        (run* [q] (all (recv-mitm 'tr q 'hello)))
      ))
    )
  )
)
