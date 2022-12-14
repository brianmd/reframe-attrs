(ns reframe-attrs.core
  (:require [reframe-attrs.core :as sut]
            [clojure.test :refer :all :as t]))

(def queries
  (macroexpand
    '(sut/build-events-and-subscriptions
       "query" "queries" :non-persist true
       ["aaaa" "bbbb"]           ;; can pass either an array of strings
       )))

;; (spit "users.txt"
;;   (with-out-str
;;     (clojure.pprint/pprint
;;       (macroexpand
;;         '(sut/build-events-and-subscriptions
;;            "user" "users" :non-persist true
;;            [{:id "id" :type :int}
;;             {:id "name" :type :str}
;;             {:id "email" :type :str}
;;             {:id "orders" :type :obj}]
;;            )))))

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
