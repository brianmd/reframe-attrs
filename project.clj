(defproject com.murphydye/reframe-attrs "0.1.28"
  :description "Macro to generate reframe events and subscriptions for attributes"
  :url "https://murphydye.com/"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]]
  :repl-options {:init-ns reframe-attrs.core}
  :deploy-repositories [["clojars" {:url           "https://repo.clojars.org"
                                    :username      :env/clojars_username
                                    :password      :env/clojars_password
                                    :sign-releases false}]
                        ["releases" :clojars]
                        ["snapshots" :clojars]]
  )
