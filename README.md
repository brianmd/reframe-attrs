[![Clojars Project](https://img.shields.io/clojars/v/com.murphydye/reframe-attrs.svg)]

# reframe-attrs

Macro to generate reframe events and subscriptions to get/set attributes in the reframe store

When you have a bunch of maps, defrecords, etc. that you need to model in re-frame, there are a lot of events and subscriptions that need to be created.
This library is intended to reduce the level of effort by generating the attribute (field) level interactions.

An important efficiency aspect of subscriptions is to chain them. Rather than get the name of user id #117 directly from the reframe store, it should get the name out of the subscription for id #117. This greatly reduces the number of subscriptions that get fired upon changes to the store.

## Design Considerations

There is only a single record for some records, such as there might be a single settings record (or map). But there are multiple records for most types. To maintain a consistent interface, all types may contain multiple records. For single record types, use an id of 1.

Both a plural name and a singular name are required. The plural name is used for dealing w/ the entire set of records, or to get/set a record w/ a given id e.g.:

* :users/clear removes all users from the database,
* :users/all returns all user records (or sets all if event),
* :users/keys returns the ids of all records,
* :users/metadata returns the values passed to build-events-and-subscriptions
* :users/store places map/record into reframe's store. It should have an :id.
* :users/id <id value> returns the user with the given id,

The singular name is used for attribute (field) access:

* :user/id returns the id with the given id, ie, returns itself if the record exists in the store
* :user/email <id> returns the email of the user w/ the given id (or sets it if event)



### Root var



### Recency


### Other event/subscriptions

This library does not (yet) attempt to generate events or subscriptions to, for example, move data between the server and client.


## Installation

`[com.murphydye/reframe-attrs "0.1.0"]`

## Usage

This UML diagram may be build with the code below.

![UML diagram](http://www.plantuml.com/plantuml/svg/SoWkIImgAStDuKhEIImkLWWjJYrIgERAJE7AIynDvKhDJSpCuQhbWihw5wN0f5CIIrAvalEBIq2oO5swTWfA-I05nKeGXLmEgNafGAC1)

(from http://www.plantuml.com/plantuml/uml/SyfFKj2rKt3CoKnELR1Io4ZDoSa70000)

```clojure
(:require
   [reframe-attrs.core]
   [reframe-attrs.persist :as persist])

 (:require-macros [reframe-attrs.core :refer [build-events-and-subscriptions]])

(build-events-and-subscriptions
  "user"                       ;; singular name (class name in UML)
  "users"                      ;; plural name
  :non-persist                 ;; root var name
  false                        ;; recency?
  [{:id "id" :type :int}       ;; fields, could also be ["id" "name" "email" "orders"]
   {:id "name" :type :str}     ;; only :id is required
   {:id "email" :type :str}
   {:id "orders" :type :obj}])

(build-events-and-subscriptions
  "order"
  "orders"
  :non-persist
  false
  ["id" "date" "cost"])       ;; the other way attrs can be defined
```

The above generates this code for the user model:
```clojure
(let*
 [all-path
  [:non-persist :users :all]
  recency-path
  [:non-persist :users :recency]]
 (re-frame.core/reg-event-db
  :users/clear
  (day8.re-frame.tracing/fn-traced
   [db _]
   (clojure.core/assoc-in db [:non-persist :users] {})))
 (re-frame.core/reg-sub
  :users/metadata
  (clojure.core/fn
   [db _]
   {:singular "user",
    :plural "users",
    :root-name :non-persist,
    :recency? true,
    :fields
    ({:id "id", :type :int}
     {:id "name", :type :str}
     {:id "email", :type :str}
     {:id "orders", :type :obj})}))
 (re-frame.core/reg-sub
  :users/all
  (clojure.core/fn
   [db _]
   (clojure.core/or (clojure.core/get-in db all-path) {})))
 (re-frame.core/reg-event-db
  :users/all
  (day8.re-frame.tracing/fn-traced
   [db [_ m]]
   (clojure.core/assoc-in db all-path m)))
 (re-frame.core/reg-sub
  :users/recency
  (clojure.core/fn
   [db _]
   (clojure.core/or (clojure.core/get-in db recency-path) [])))
 (re-frame.core/reg-sub
  :users/keys
  :<-
  [:users/all]
  (clojure.core/fn [all] (clojure.core/keys all)))
 (re-frame.core/reg-event-db
  :users/store
  (day8.re-frame.tracing/fn-traced
   [db [_ m]]
   (clojure.core/let
    [id
     (if
      (:id m)
      (if
       (clojure.core/string? (:id m))
       (clojure.core/keyword (:id m))
       (:id m))
      (persist.core/next-id :users))
     path
     (reframe-attrs.core/all-path-for :non-persist :users id)
     m
     (if
      (:id m)
      (clojure.core/merge
       (clojure.core/get-in
        db
        (reframe-attrs.core/all-path-for :non-persist :users id))
       m)
      (clojure.core/assoc m :id id))]
    (clojure.core/->
     db
     (clojure.core/assoc-in
      (reframe-attrs.core/all-path-for :non-persist :users id)
      m)
     (clojure.core/update-in
      recency-path
      (clojure.core/partial reframe-attrs.core/update-recency id))))))
 (re-frame.core/reg-sub
  :users/id
  :<-
  [:users/all]
  (clojure.core/fn [users [_ id]] (clojure.core/get users id)))
 (re-frame.core/reg-sub
  :users/users
  :<-
  [:users/all]
  :<-
  [:users/recency]
  (clojure.core/fn
   [[all recency]]
   (clojure.core/mapv (clojure.core/fn [x] (all x)) recency)))
 (re-frame.core/reg-event-db
  :users/bump-recency
  (day8.re-frame.tracing/fn-traced
   [db [_ id]]
   (clojure.core/update-in
    db
    recency-path
    (clojure.core/partial reframe-attrs.core/update-recency id))))
 (do
  (re-frame.core/reg-sub
   :users/id
   (clojure.core/fn [_] (re-frame.core/subscribe [:users/users]))
   (clojure.core/fn
    [all-vector fld]
    (clojure.core/mapv
     (clojure.core/fn [x] ((clojure.core/keyword "id") x))
     all-vector)))
  (re-frame.core/reg-sub
   :users/name
   (clojure.core/fn [_] (re-frame.core/subscribe [:users/users]))
   (clojure.core/fn
    [all-vector fld]
    (clojure.core/mapv
     (clojure.core/fn [x] ((clojure.core/keyword "name") x))
     all-vector)))
  (re-frame.core/reg-sub
   :users/email
   (clojure.core/fn [_] (re-frame.core/subscribe [:users/users]))
   (clojure.core/fn
    [all-vector fld]
    (clojure.core/mapv
     (clojure.core/fn [x] ((clojure.core/keyword "email") x))
     all-vector)))
  (re-frame.core/reg-sub
   :users/orders
   (clojure.core/fn [_] (re-frame.core/subscribe [:users/users]))
   (clojure.core/fn
    [all-vector fld]
    (clojure.core/mapv
     (clojure.core/fn [x] ((clojure.core/keyword "orders") x))
     all-vector))))
 (do
  (re-frame.core/reg-sub
   :user/id
   (clojure.core/fn
    [[the-name id]]
    (re-frame.core/subscribe [:users/id id]))
   (clojure.core/fn
    [obj [query-v id]]
    ((clojure.core/keyword "id") obj)))
  (re-frame.core/reg-sub
   :user/name
   (clojure.core/fn
    [[the-name id]]
    (re-frame.core/subscribe [:users/id id]))
   (clojure.core/fn
    [obj [query-v id]]
    ((clojure.core/keyword "name") obj)))
  (re-frame.core/reg-sub
   :user/email
   (clojure.core/fn
    [[the-name id]]
    (re-frame.core/subscribe [:users/id id]))
   (clojure.core/fn
    [obj [query-v id]]
    ((clojure.core/keyword "email") obj)))
  (re-frame.core/reg-sub
   :user/orders
   (clojure.core/fn
    [[the-name id]]
    (re-frame.core/subscribe [:users/id id]))
   (clojure.core/fn
    [obj [query-v id]]
    ((clojure.core/keyword "orders") obj))))
 (do
  (re-frame.core/reg-event-db
   :user/id
   (day8.re-frame.tracing/fn-traced
    [db [_ id value]]
    (clojure.core/let
     [path
      (reframe-attrs.core/all-path-for
       :non-persist
       :users
       id
       (clojure.core/keyword "id"))]
     (clojure.core/assoc-in db path value))))
  (re-frame.core/reg-event-db
   :user/name
   (day8.re-frame.tracing/fn-traced
    [db [_ id value]]
    (clojure.core/let
     [path
      (reframe-attrs.core/all-path-for
       :non-persist
       :users
       id
       (clojure.core/keyword "name"))]
     (clojure.core/assoc-in db path value))))
  (re-frame.core/reg-event-db
   :user/email
   (day8.re-frame.tracing/fn-traced
    [db [_ id value]]
    (clojure.core/let
     [path
      (reframe-attrs.core/all-path-for
       :non-persist
       :users
       id
       (clojure.core/keyword "email"))]
     (clojure.core/assoc-in db path value))))
  (re-frame.core/reg-event-db
   :user/orders
   (day8.re-frame.tracing/fn-traced
    [db [_ id value]]
    (clojure.core/let
     [path
      (reframe-attrs.core/all-path-for
       :non-persist
       :users
       id
       (clojure.core/keyword "orders"))]
     (clojure.core/assoc-in db path value))))))
```

## License

Copyright © 2022

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
