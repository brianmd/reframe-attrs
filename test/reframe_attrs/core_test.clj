(ns reframe-attrs.core
  (:require [reframe-attrs.core :as sut]
            [clojure.test :refer :all :as t]))

(def queries
  (macroexpand
    '(sut/build-events-and-subscriptions
       "query" "queries" :non-persist true
       ["aaaa" "bbbb"]           ;; can pass either an array of strings
       )))

(defn extract-events
  [body]
  (reduce (fn [accum x]
            (if (= 'do (first x))
              (concat accum (rest x))
              (conj accum x)))
          []
          body))

(deftest macro-test
  (let [body (-> queries rest rest)
        events (extract-events body)

        fns (set (map first events))
        expected-fns #{'re-frame.core/reg-event-db 're-frame.core/reg-sub}

        names (set (map second events))
        expected-names
        #{:queries/all :queries/recency :queries/bump-recency
          :queries/clear :queries/metadata
          :queries/queries :queries/keys
          :queries/id :queries/store
          :queries/aaaa :queries/bbbb
          :query/aaaa :query/bbbb}]
    (is (= expected-fns fns))
    (is (= expected-names names))
    ))
