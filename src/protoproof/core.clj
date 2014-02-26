(ns protoproof.core
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
