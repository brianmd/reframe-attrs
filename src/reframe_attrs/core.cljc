(ns reframe-attrs.core
  (:require
   [clojure.string :refer [join]]
  ;;   ;; [re-frame.core :as rf]
  ;;   ;; [day8.re-frame.tracing :refer-macros [fn-traced]]
   )
  )

(defmacro build-events-and-subscriptions
  [singular-name plural-name root-name recency? fields]
  (let [fields (map #(if (string? %) {:id %} %) fields)
        singular (keyword singular-name)
        plural (keyword plural-name)
        singular-keyword (fn [& [fld]]  (keyword singular-name fld))
        plural-keyword   (fn [& [fld]] (keyword plural-name fld))
        plural-param (symbol plural-name)
        d 'db
        field-names (map :id fields)
        id 'id
        ignore '_
        m 'm
        metadata {:singular singular-name
                  :plural plural-name
                  :root-name root-name
                  :recency? recency?
                  :fields fields}
        ]
    `(let
       [~'all-path [~root-name ~plural :all]
        ~'recency-path ~(if recency? `[~root-name ~plural :recency])]

       (re-frame.core/reg-event-db
        ~(plural-keyword "clear")
        (day8.re-frame.tracing/fn-traced
         [~d ~ignore]
         (assoc-in ~d [~root-name ~plural] {})
         ))

       (re-frame.core/reg-sub
        ~(plural-keyword "metadata")
        (fn [~d ~ignore]
          ~metadata
          ))

       (re-frame.core/reg-sub
        ~(plural-keyword "all")
        (fn [~d ~ignore]
          (or (get-in ~d ~'all-path) {})))

       (re-frame.core/reg-event-db
        ~(plural-keyword "all")
        (day8.re-frame.tracing/fn-traced
         [~d [~ignore ~m]]
         (assoc-in ~d ~'all-path ~m)
         ))

       ~(if recency?
          `(re-frame.core/reg-sub
            ~(plural-keyword "recency")
            (fn [~d ~ignore]
              (or (get-in ~d ~'recency-path) []))))

       (re-frame.core/reg-sub
         ~(plural-keyword "keys")
         :<- [~(plural-keyword "all")]
        (fn [~'all]
          (keys ~'all)))

       ;; store
       ;; TODO: how deal with additional requirements, such as ensuring a data-id exists in queries?
       (re-frame.core/reg-event-db
        ~(plural-keyword "store")
        (day8.re-frame.tracing/fn-traced
         [~d [~ignore ~m]]

         (let [
               ~'id (if (:id ~m)
                      (if (string? (:id ~m))
                        (keyword (:id ~m))
                        (:id ~m))
                      (persist.core/next-id ~plural))
               ~'path (all-path-for ~root-name ~plural ~'id)
               ~m (if (:id ~m)
                   (merge (get-in ~d (all-path-for ~root-name ~plural ~'id)) ~m)
                   (assoc ~m :id ~'id))

               ]
           (->
            ~d
            (assoc-in (all-path-for ~root-name ~plural ~'id) ~m)
            ~(if recency?
               `(update-in ~'recency-path (partial update-recency ~'id))
               `identity
               )
            ))
         ))

       ;; get by id     NOTE: this is :queries/id rather than :queries/query
       (re-frame.core/reg-sub
        ~(plural-keyword "id")
        :<- [~(plural-keyword "all")]
        (fn [~plural-param [~ignore ~id]]
          (get ~plural-param ~id)
          ))

       ;; returns vector sorted by recency, eg, :charts/charts
       ~(if recency?
          `(re-frame.core/reg-sub
            ~(plural-keyword plural-name)
            :<- [~(plural-keyword "all")]
            :<- [~(plural-keyword "recency")]
            (fn [[~'all ~'recency]]
              (mapv (fn [~'x] (~'all ~'x)) ~'recency)))

          `(re-frame.core/reg-sub
            ~(plural-keyword plural-name)
            :<- [~(plural-keyword "all")]
            (fn [~'all ~ignore]
              (mapv identity ~'all)
              ))
          )

         ~(if recency?
            `(re-frame.core/reg-event-db
              ~(plural-keyword "bump-recency")
              (day8.re-frame.tracing/fn-traced
                [~d [~ignore ~id]]
                (update-in ~d ~'recency-path (partial update-recency ~id)))))


         ;; subscriptions to get all of each field
         (do
           ~@(doall
             (map (fn [fld]
                    `(re-frame.core/reg-sub
                      ~(plural-keyword fld)
                      (fn [~ignore]
                        (re-frame.core/subscribe [~(plural-keyword plural-name)]))
                      (fn [~'all-vector ~'fld]
                        (mapv (fn [~'x] ((keyword ~fld) ~'x)) ~'all-vector))))
                  field-names))
            )

         (do
           ~@(doall
              (map (fn [fld]
                     `(re-frame.core/reg-sub
                       ~(singular-keyword fld)
                       (fn [[~'the-name ~id]]   ;; eg [:charts/chart-name 1]
                         (re-frame.core/subscribe [~(plural-keyword "id") ~id]))
                       (fn [~'obj [~'query-v ~id]]
                         ((keyword ~fld) ~'obj))))
                       ;; ))
                   field-names)))

         ;; getter events for each field of a record,
         ;; eg, [:chart/query-id 1] gets query-id for chart id 1
         (do
           ~@(doall
             (map (fn [fld]
                    `(re-frame.core/reg-event-db
                      ~(singular-keyword fld)
                      (day8.re-frame.tracing/fn-traced
                       [~d [~ignore ~id ~'value]]
                       (let [~'path (all-path-for ~root-name ~plural ~id (keyword ~fld))]
                         (assoc-in ~d ~'path ~'value)))))
                  field-names)))
         )))
