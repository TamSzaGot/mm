(ns backend.site
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]]
            [ring.util.http-response :refer [ok content-type]]
    ;            [hiccup.page :refer [html5 include-js include-css]]
            [hiccup.page :refer [html5 include-js]]))

(def app
  (html
    (html5
      [:head
       [:title "mm"]
       [:meta {:charset "utf-8"}]
       [:meta {:description "mm"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
       [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
       [:link {:type "text/css" :href "css/mm.css" :rel "stylesheet"}]
       [:body
        [:div#main]
        (include-js "js/app.js")]
       ]
      )))

(defroutes site-routes
           (GET "/" [] (-> (ok app) (content-type "text/html; charset=utf-8")))
;           (GET "/" [] (slurp (io/resource "index.html")))
           (route/resources "/fonts" {:root "fonts"})
           (route/resources "/js" {:root "js"})
           (route/resources "/css" {:root "css"})
           (route/resources "/images" {:root "images"})
           (route/not-found (slurp (io/resource "404.html"))))

