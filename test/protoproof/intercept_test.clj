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
    [intercepter 'tr 'Mallory]
    [sendmsg 'tr 'Bob 'Alice 'def2]
    [dropm 'tr 'Mallory 'abc]
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

(deftest dropmessage-test
  (testing "Mallory can drop a message"
    (is (= '() (
      with-dbs [users protocol dropmessage]
        (run* [q] (all (recv-mitm 'tr 'Bob q)))
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

(deftest dropmessage-knows-abc-test
  (testing "Mallory can drop a message"
    (is (= '(Alice Eve Mallory) (
      with-dbs [users protocol dropmessage]
        (run* [q] (all (knows q 'abc)))
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


(deftest replacemessage-recv-abc-test
  (testing "Mallory can replace a message"
    (is (= '() (
      with-dbs [users protocol replacemessage]
        (run* [q] (all (recv-mitm 'tr q 'abc)))
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

(deftest replacemessage-knows-hello-test
  (testing "Mallory can replace a message"
    (is (= '(Mallory Bob) (
      with-dbs [users protocol replacemessage]
        (run* [q] (all (knows q 'hello)))
      ))
    )
  )
)

(deftest replacemessage-knows-abc-test
  (testing "Mallory can replace a message"
    (is (= '(Alice Eve Mallory) (
      with-dbs [users protocol replacemessage]
        (run* [q] (all (knows q 'abc)))
      ))
    )
  )
)
