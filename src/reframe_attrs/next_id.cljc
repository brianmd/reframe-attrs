(ns reframe-attrs.next-id)

(def next-ids (atom {}))

(defn reset-next-ids!
  []
  (reset! next-ids {}))

(defn next-id
  ([] (next-id :global))
  ([name]
   (swap! next-ids update name (fnil inc 0))))
