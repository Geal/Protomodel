(ns protoproof.respond-test
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

(db-rel successor previous message)

(def protocol
  (db
    [transport 'tr]
    [generates 'Alice 'g]
    [generates 'Bob 'g]
    [generates 'Alice 'abc]
    [generates 'Bob 'def2]
    [sendmsg 'tr 'Alice 'Bob 'abc]
    [successor 'abc 'def2]
  )
)

(def respondmsg
  (tabled [tr alice bob message]
    (conde
      [
        (fresh[previous]
          (sentmessage tr bob alice previous)
          (generates alice message)
          (successor previous message)
        )
      ]
    )
  )
)

(def knows
  (tabled [u x]
    (conde
      [(generates u x)]
      [(know-transport-mitm knows u x)]
    )
  )
)

(deftest respondmessage-test
  (testing "what does Bob answer?"
    (is (= '(def2) (
      with-dbs [users protocol]
        (run* [q] (all (respondmsg 'tr 'Bob 'Alice q)))
      ))
    )
  )
)

(deftest respondmessage-recv-test
  (testing "Bob can answer to a message"
    (is (= '(Alice) (
      with-dbs [users protocol]
        (run* [q] (all (recv-mitm 'tr q 'def2)))
      ))
    )
  )
)

(deftest respondmessage-knows-test
  (testing "Bob can answer to a message"
    (is (= '(abc g def2) (
      with-dbs [users protocol]
        (run* [q] (all (knows 'Alice q)))
      ))
    )
  )
)

