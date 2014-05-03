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
      [(knowc knows u x)]
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

(def symmetric-crypto
  (db
    [generates 'Alice 'abc]
    [generates 'Alice 'k]
    [sym-enc 'aes 'k 'abc 'ciphertext]
  )
)

(deftest sym-enc-test
  (testing "Symmetric encryption"
    (is (= '(Alice)
            (with-dbs [symmetric-crypto]
              (run* [q] (all (knows q 'ciphertext))))
        )
    )
  )
)

(def symmetric-crypto-bob
  (db
    [sym-enc 'aes 'k 'abc 'ciphertext]
    ; smulate knowledge without worrying about transmission
    [generates 'Bob 'k]
    [generates 'Bob 'ciphertext]
  )
)

(deftest sym-dec-test
  (testing "Symmetric decryption"
    (is (= '(Bob)
            (with-dbs [symmetric-crypto-bob]
              (run* [q] (all (knows q 'plaintext))))
        )
    )
  )
)

(def asymmetric-encryption
  (db
    [generates 'Alice 'abc]
    ;[generates 'Alice 'priv]
    [generates 'Alice 'pub]
    [asym-keys 'rsa 'priv 'pub]
    [asym-enc 'rsa 'pub 'abc 'ciphertext]
  )
)

(deftest sym-enc-test
  (testing "Asymmetric encryption"
    (is (= '(Alice)
            (with-dbs [asymmetric-encryption]
              (run* [q] (all (knows q 'ciphertext))))
        )
    )
  )
)

(def asymmetric-decryption
  (db
    [asym-keys 'rsa 'priv 'pub]
    [asym-enc 'rsa 'pub 'plaintext 'ciphertext]
    [generates 'Bob 'priv]
    [generates 'Bob 'pub]
    [generates 'Bob 'ciphertext]
  )
)

(deftest sym-dec-test
  (testing "Asymmetric decryption"
    (is (= '(Bob)
            (with-dbs [asymmetric-decryption]
              (run* [q] (all (knows q 'plaintext))))
        )
    )
  )
)
