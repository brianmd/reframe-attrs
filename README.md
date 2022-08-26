[![Clojars Project](https://img.shields.io/clojars/v/com.murphydye/reframe-attrs.svg)]

# reframe-attrs

Macro to generate reframe events and subscriptions to get/set attributes in the reframe store

## Installation

`[com.murphydye/reframe-attrs "0.1.0"]`

## Usage

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
  [{:id "name" :type :str}     ;; fields, could also be ["name" "email"]
   {:id "email" :type :str}])
```

![UML diagram](http://www.plantuml.com/plantuml/svg/SoWkIImgAStDuKhEIImkLWWjJYrIgERAJE7AIynDvKhDJSpCuQhbWihw5wN0f5CIIrAvalEBIq2oO5swTWfA-I05nKeGXLmEgNafGAC1)

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
