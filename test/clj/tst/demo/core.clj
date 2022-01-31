(ns tst.demo.core
  (:use demo.core tupelo.core tupelo.test)
  (:require
    [schema.core :as s]
    [tupelo.string :as str]
    [tupelo.schema :as tsk])
  (:import
    [java.lang System]
    [java.util Objects]))

; Plumatic Schema validates that a data value matches a type
(dotest
  ; Integer example
  (is= 2 (s/validate s/Int 2))
  (throws? (s/validate s/Int 2.3))

  ; s/Num is floating-point or integer
  (is= 3 (s/validate s/Num 3))
  (is= 3.5 (s/validate s/Num 3.5))

  ; String example
  (is= "abc" (s/validate s/Str "abc"))
  (throws? (s/validate s/Str 3))

  ; Boolean example
  (is= true (s/validate s/Bool true))
  (is= false (s/validate s/Bool false))
  (throws? (s/validate s/Bool 3))

  ; Note that `nil` in Clojure source code is identical to `null` in Java source code
  (let [nilobj   nil
        java-str (with-system-out-str ; capture output from System.out.print
                   (.print System/out ^java.lang.Object nilobj))
        clj-str  (with-out-str ; capture output from clojure.core/pr
                   (pr nilobj))]
    ; string results captured from printing functions
    (is= "null" java-str)
    (is= "nil" clj-str)

    ; result us using predicate function
    (is= true (nil? nilobj))
    (is= true (Objects/isNull nilobj)))

  ; nil values fail for any type
  (throws? (s/validate s/Int nil))
  (throws? (s/validate s/Num nil))
  (throws? (s/validate s/Str nil))
  (throws? (s/validate s/Bool nil))

  ; using (s/maybe <type>) means nil values are also allowed
  (is= nil (s/validate (s/maybe s/Int) nil))
  (is= nil (s/validate (s/maybe s/Num) nil))
  (is= nil (s/validate (s/maybe s/Str) nil))
  (is= nil (s/validate (s/maybe s/Bool) nil)))

; use s/cond-pre to offer a choice of types
(def Int-or-Str (s/cond-pre s/Int s/Str))
(def Int-or-Str-or-nil (s/maybe (s/cond-pre s/Int s/Str)))
(dotest
  (is= 42 (s/validate Int-or-Str 42))
  (is= "forty-two" (s/validate Int-or-Str "forty-two"))
  (throws? (s/validate Int-or-Str nil)
    (throws? (s/validate Int-or-Str 4.2)))

  (is= 42 (s/validate Int-or-Str-or-nil 42))
  (is= "forty-two" (s/validate Int-or-Str-or-nil "forty-two"))
  (is= nil (s/validate Int-or-Str-or-nil nil))
  (throws? (s/validate Int-or-Str-or-nil 4.2)))

(def All-or-None-Str (s/enum "all" "none")) ; must be one of the listed values
(def IntList [s/Int])
(def IntListMaybe [s/Int])
(dotest
  (is= "all" (s/validate All-or-None-Str "all"))
  (is= "none" (s/validate All-or-None-Str "none"))
  (throws? (s/validate All-or-None-Str "other"))

  (is= [] (s/validate IntList []))
  (is= [1] (s/validate IntList [1]))
  (is= [1 2] (s/validate IntList [1 2]))
  (throws? (s/validate IntList "abc"))
  (throws? (s/validate IntList 5))
  (isnt (s/validate IntList nil)) ; *** unexpected *** doesn't throw like others

  (is= [] (s/validate IntListMaybe []))
  (is= [1] (s/validate IntListMaybe [1]))
  (is= [1 2] (s/validate IntListMaybe [1 2]))
  (throws? (s/validate IntListMaybe "abc"))
  (throws? (s/validate IntListMaybe 5))
  (is= nil (s/validate IntListMaybe nil))
  )

(def All-or-None (s/enum :all :none)) ; must be one of the listed values
(def IntList [s/Int])
(def IntListMaybe (s/maybe [s/Int]))
(dotest
  (is= :all (s/validate All-or-None :all))
  (is= :none (s/validate All-or-None :none))
  (throws? (s/validate All-or-None :other))

  (is= [] (s/validate IntList []))
  (is= [1] (s/validate IntList [1]))
  (is= [1 2] (s/validate IntList [1 2]))
  (throws? (s/validate IntList "abc"))
  (throws? (s/validate IntList 5))
  (isnt (s/validate IntList nil)) ; *** unexpected *** doesn't throw like others

  (is= [] (s/validate IntListMaybe []))
  (is= [1] (s/validate IntListMaybe [1]))
  (is= [1 2] (s/validate IntListMaybe [1 2]))
  (throws? (s/validate IntListMaybe "abc"))
  (throws? (s/validate IntListMaybe 5))
  (is= nil (s/validate IntListMaybe nil))
  )

(dotest
  (is= 5 (s/validate s/Int 5))
  (throws? (s/validate s/Int nil)) ; s/validate throws on failure (nil value)

  (is= 5 (s/validate (s/maybe s/Int) 5))
  (is= nil (s/validate (s/maybe s/Int) nil)) ; s/maybe allows nil values to succedd

  (is= [] (s/validate [s/Int] []))
  (is= [5] (s/validate [s/Int] [5]))
  (is= nil (s/validate [s/Int] nil)) ; ***** does not throw for nil values, even w/o s/maybe! *****
  )

(def IntSet #{s/Int})
(def IntSet-or-Keyword (s/cond-pre #{s/Int} s/Keyword))
(dotest
  (is= #{1} (s/validate IntSet #{1}))
  (throws? (s/validate IntSet :foo))
  (is= #{1} (s/validate IntSet-or-Keyword #{1}))
  (is= :aa (s/validate IntSet-or-Keyword :aa)))

;-----------------------------------------------------------------------------
; NORMAL USAGE: use plumatic/schema to document (& enforce) arg & return types
(dotest
  (is= 5 (add-int-2 2 3)) ; add 2 ints to return an int
  (throws? (add-int-2 2.0 3.0)) ; double arg values fail Plumatic Schema

  (is= 5 (add-num-2 2 3)) ; `s/Num` works for doubles and ints
  (is= 5.0 (add-num-2 2.0 3.0)) ; `s/Num` works for doubles and ints

  (is= 3 (int-div-by-2 6)) ; arg & return val are both integers
  (throws? (int-div-by-2 7)) ; return val is not an integer

  ; disable Schema checking temporarily
  (tsk/with-validation-disabled
    (is= 5.0 (add-int-2 2.0 3.0)) ; double arg values fail Plumatic Schema
    (is= 7/2 (int-div-by-2 7))) ; return val is a clojure.lang/Rational, not an integer

  (throws? (int-div-by-2 7))) ; Schema is now enforced again

;-----------------------------------------------------------------------------
; old stuff #todo cleanup

(dotest
  (def List-of-Maps  [{}])
  (is= [{} {}] (s/validate List-of-Maps [{} {}]))

  ;validating array with key and map, not work
  (is= [0 {}]
       (s/validate
         [(s/one s/Int "int-1")
          (s/one {} "map-2")]
         [0 {}]))

  ;validating array with key and map, not work
  (is= [0 {} 1 {}]
       (s/validate
         [(s/one s/Int "int-1")
          (s/one {} "map-2")
          (s/one s/Int "int-3")
          (s/one {} "map-4")]
         [0 {} 1 {}])))

(defn validate-alternate-int-and-map
      [some-seq]
      (let [pairs   (partition 2 some-seq)
            all-int (mapv first pairs)
            all-map (mapv second pairs)]
        (assert (= (count all-map)
                   (count all-int)))
        (s/validate [s/Int] all-int)
        (s/validate [{}] all-map)
        some-seq))

(dotest
  (is= [0 {}]            (validate-alternate-int-and-map [0 {}]))
  (is= [0 {} 1 {}]       (validate-alternate-int-and-map [0 {} 1 {}]))
  (is= [0 {} 1 {} 2 {}]  (validate-alternate-int-and-map [0 {} 1 {} 2 {}]))
  )