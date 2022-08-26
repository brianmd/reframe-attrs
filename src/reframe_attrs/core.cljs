(ns reframe-attrs.core)

;; Macros are created on the clojure side, but the functions generated
;; in the build-events-and-subscriptions macro need to be available
;; in clojurescript.

(defn all-path-for
  ([persistance plural-name id]
   [persistance plural-name :all id])
  ([persistance plural-name id fld]
   [persistance plural-name :all id fld])
  )

(defn update-recency
  [id s]
  (if (and s (= id (first s)))
    s
    (conj
      (if (nil? s)
        '()
        (remove #(= % id) s))
      id)))
