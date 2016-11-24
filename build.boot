(set-env!
 :source-paths    #{"src/cljs"}
 :resource-paths  #{"src/clj" "src/java" "resources"}
 :dependencies '[[org.clojure/clojure    "1.9.0-alpha12"]
                 [org.clojure/clojurescript "1.9.229"]
                 [boot/core                 "2.6.0"      :scope "test"]
                 [adzerk/boot-cljs          "1.7.228-1"  :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.0"      :scope "test"]
                 [adzerk/boot-reload        "0.4.8"      :scope "test"]
                 [pandeiro/boot-http        "0.7.2"      :scope "test"]
                 [com.cemerick/piggieback   "0.2.1"      :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.12"     :scope "test"]
                 [weasel                    "0.7.0"      :scope "test"]

                 ; Frontend
;                 [org.clojure/clojurescript "1.7.228"]
                 [rum "0.10.4"]

                 ; Backend
                 [http-kit "2.2.0"]
                 [reloaded.repl "0.2.3"]
                 [cheshire "5.3.1"]
                 [ring "1.5.0"]
                 [metosin/ring-http-response "0.8.0"]
                 [compojure "1.5.1"]
                 [hiccup "1.0.5"]
                 ]
)

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
; '[backend.boot          :refer [start-app]]
 '[backend.main          :refer [site-and-api]]
 '[reloaded.repl         :refer [go reset start stop system]]
 )

(task-options!
  pom {:project 'mm
       :version "0.1.0"
       :description "MinMyndighetspost sample clojure implementation"
       :license {"The MIT License (MIT)" "http://opensource.org/licenses/mit-license.php"}}
  aot {:namespace #{'mm.main}}
  jar {:main 'backend.main}
  cljs {:source-map true})

(deftask build []
  (comp (speak)
        (cljs)
        ))

(deftask run []
  (comp (serve :handler 'backend.main/site-and-api :reload true :httpkit true)
        (watch)
        (cljs-repl)
        (reload)
        (build)
        ))

(deftask production []
  (task-options! cljs {:optimizations :advanced})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none :source-map true}
                 reload {:on-jsload 'mm.app/init})
  identity)

(deftask dev
  "Start the dev env..."
  [p port       PORT int  "Port for web server"]
  (comp (development)
        (run)
;        (start-app :port port)
        ))

(deftask package
  "Builds an uberjar of this project that can be run with java -jar"
  []
  (comp
    (cljs :optimizations :advanced
          :compiler-options {:preloads nil})
    (javac)
    (pom :project 'myproject
        :version "1.0.0")
    (uber)
    (jar :main 'main.Main)
    (sift :include #{#"project.jar"})
    (target)))

(defn -main [& args]
  (require 'backend.main)
  (apply (resolve 'backend.main/-main) args))