(ns protoproof.intercept-test
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
    ;[power 'g 'abc 'y]
  )
)

(def knows
  (tabled [u x]
    (conde
      [(generates u x)]
      [(know-transport-mitm u x)]
      ;[(knowc u x)]
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
  (testing "Mallory can drop a message"
    (is (= '() (
      with-dbs [users protocol dropmessage]
        (run* [q] (all (recv-mitm 'tr q 'abc)))
      ))
    )
  )
)

(deftest dropmessage-knows-test
  (testing "Mallory can drop a message"
    (is (= '(g def2) (
      with-dbs [users protocol dropmessage]
        (run* [q] (all (knows 'Bob q)))
      ))
    )
  )
)

(def passmessage
  (db
    ; intercepter does not let the message pass, it is dropped
    [intercepter 'tr 'Mallory]
    [pass 'tr 'Mallory 'abc]
  )
)

(deftest passmessage-test
  (testing "Mallory can let a message pass"
    (is (= '(Bob) (
      with-dbs [users protocol passmessage]
        (run* [q] (all (recv-mitm 'tr q 'abc)))
      ))
    )
  )
)

(deftest passmessage-knows-test
  (testing "Mallory can let a message pass"
    (is (= '(g def2 abc) (
      with-dbs [users protocol passmessage]
        (run* [q] (all (knows 'Bob q)))
      ))
    )
  )
)

(def replacemessage
  (db
    [intercepter 'tr 'Mallory]
    [generates 'Mallory 'hello]
    [mitm 'tr 'Mallory 'abc 'hello]
  )
)

(deftest replacemessage-test
  (testing "Mallory can replace a message"
    (is (= '(Bob) (
      with-dbs [users protocol replacemessage]
        (run* [q] (all (recv-mitm 'tr q 'hello)))
      ))
    )
  )
)

(deftest replacemessage-knows-test
  (testing "Mallory can replace a message"
    (is (= '(g def2 hello) (
      with-dbs [users protocol replacemessage]
        (run* [q] (all (knows 'Bob q)))
      ))
    )
  )
)
