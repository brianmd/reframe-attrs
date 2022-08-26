(ns reframe-attrs.persist
  (:require
   [re-frame.core :as rf]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reframe-attrs.next-id :refer :all]
   ))

;; TODO: create next-ids event/subscription <-> localStorage

(def default-persist-name "persist")

(defn get-persisted
  ([] (get-persisted default-persist-name))
  ([store-name]
   (into (sorted-map)
         (some->> (.getItem js/localStorage (name store-name))
                  (cljs.reader/read-string)))))

(defn store-persisted
  ([obj] (store-persisted default-persist-name obj))
  ([store-name obj]
   (.setItem js/localStorage (name store-name) (str obj))))

(rf/reg-sub
 :persist/next-id
 (fn [_db [_ name]]
   (next-id name)))

(rf/reg-event-db
 :persist/clear
 (fn-traced
  [_ [_ name]]
  (.removeItem js/localStorage (or name default-persist-name))))

(rf/reg-sub
 :persist/get
 (fn
  [db [_ name]]
  (get-persisted name)))

(rf/reg-event-db
 :persist/load
 (fn-traced
  [db [_ name]]
  (assoc db :persist
          (get-persisted name))))

(rf/reg-event-db
  :persist/store
  (fn-traced
    [db [_ store-name]]
    (let [store-name (or store-name default-persist-name)]
      (store-persisted store-name (str (get db (keyword store-name)))))))

;; TODO: want pure functions, so use something like the following in the future:

;; (rf/reg-event-fx
;;  :persist/store
;;  ;; save both the persist portion of db AND nexti-ids
;;  )

;; (rf/reg-event-fx
;;  :persist/load
;;  ;; save both the persist portion of db AND nexti-ids
;;  )

;; from https://github.com/gothinkster/clojurescript-reframe-realworld-example-app/blob/master/src/conduit/db.cljs
;; (rf/reg-cofx
;;  :persist/loaddddddd
;;  (fn [cofx _]
;;    (assoc cofx :local-store-user  ;; put the local-store user into the coeffect under :local-store-user
;;           (into (sorted-map)      ;; read in user from localstore, and process into a sorted map
;;                 (some->> (.getItem js/localStorage default-persist-name)
;;                          (cljs.reader/read-string))))))  ;; EDN map -> map


;; (defn set-user-ls
;;   "Puts user into localStorage"
;;   [user]
;;   (.setItem js/localStorage conduit-user-key (str user)))  ;; sorted-map written as an EDN map

;; ;; Removes user information from localStorge when a user logs out.
;; ;;
;; (defn remove-user-ls
;;   "Removes user from localStorage"
;;   []
;;   (.removeItem js/localStorage conduit-user-key))
