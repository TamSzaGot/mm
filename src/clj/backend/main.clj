(ns backend.main
  (:use [org.httpkit.server :only [run-server]])
  (:require [backend.api :refer [api-routes]]
            [backend.site :refer [site-routes]]
            [compojure.core :refer [routes]]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as jetty]))

; Combine the site and rest-api
(def site-and-api (routes api-routes site-routes  ))

(def site-handler (handler/site site-and-api))

(defn -main [& args]
  ;  (jetty/run-jetty (handler/site site-and-api) {:port 8080})
  (run-server (handler/site site-and-api) {:port 8080})
  )
