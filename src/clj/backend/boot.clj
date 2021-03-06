(ns backend.boot
  {:boot/export-tasks true}
  (:require [boot.core :as b]
            [reloaded.repl :refer [go]]
            [backend.main :refer :all]
            [clojure.tools.namespace.repl :refer [disable-reload!]]))

(disable-reload!)

(comment
(b/deftask start-app
  [p port   PORT int  "Port"]
  (let [x (atom nil)]
    (b/with-pre-wrap fileset
      (swap! x (fn [x]
                  (if x
                    x
;                    (do (setup-app! {:port port})
                    (do (site-and-api)
                        (go)))))
      fileset)))
)

(b/deftask start-app
  [p port   PORT int  "Port"]
    (b/with-pre-wrap fileset (-main) fileset))
