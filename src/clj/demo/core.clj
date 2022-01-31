(ns demo.core
  (:use tupelo.core)
  (:require
    [schema.core :as s]))


(s/defn add-int-2 :- s/Int
  "Adds 2 integers"
  [a :- s/Int
   b :- s/Int]
  (+ a b))

(s/defn add-num-2 :- s/Num
  "Adds 2 numbers (int or float)"
  [a :- s/Num
   b :- s/Num]
  (+ a b))

(s/defn int-div-by-2 :- s/Int
  [a :- s/Int]
  (/ a 2))

(defn -main
  [& args]
  (println "Hello, World! Again!"))

