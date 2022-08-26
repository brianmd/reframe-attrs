(ns reframe-attrs.next-id-test
  (:require [reframe-attrs.next-id :as sut]
            [clojure.test :as t])
  )

(t/deftest next-id-test
  (sut/reset-next-ids!)
  (sut/next-id)
  (sut/next-id :abc)
  (sut/next-id)
  (sut/next-id)
  (t/is (= @sut/next-ids {:global 3 :abc 1}))
  )
