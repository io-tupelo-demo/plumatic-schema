(ns tst.demo.core
  (:use tupelo.core tupelo.test)
  (:require
    [schema.core :as s]))

(dotest
  (is= [{} {}]
       (spyx (s/validate
               [{}]
               [{} {}])))

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