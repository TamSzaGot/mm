;==============================================================================================================
; MinMyndighetspost (partial) ClojureScript implementation with rum React wrapper
;==============================================================================================================
(ns mm.app
  (:require
    [rum.core :as rum]
    [clojure.string :as string])
)

(declare update-messages)
(declare update-main)

;==============================================================================================================
; DATA
;==============================================================================================================
(def user {:name "Tamas Edvin Szabo" :pnr "19630223‑9378"})

(def msg-state (atom {:messages [
  {:id 1 :date "2016-09-28T08:24:31+00:00" :title "Test 1" :sender "Skatteverket" :read true}
  {:id 2 :date "2016-10-08T18:14:43+00:00" :title "Test 2" :att "true" :sender "Skatteverket" :read true}
  {:id 3 :date "2016-10-21T13:07:00+00:00" :title "First message" :sender "Skatteverket" :favorite true}
  {:id 4 :date "2016-10-22T23:53:06+00:00" :title "Second message" :att "true" :sender "Skatteverket" :read true}
  {:id 5 :date "2016-10-23T03:37:03+00:00" :title "Third message" :sender "Bolagsverket"}
  {:id 6 :date "2016-10-24T09:18:41+00:00" :title "Fourth message - try a long one!" :sender "CSN" :favorite true}
  {:id 7 :date "2016-11-02T03:10:03+00:00" :title "Ny firma registrerad" :sender "Bolagsverket"}
  {:id 8 :date "2016-11-03T09:27:03+00:00" :title "Välkommen" :sender "Bolagsverket"}
  {:id 9 :date "2016-11-10T11:33:03+00:00" :title "Vidare information" :sender "Bolagsverket"}
]}))

(def folder-state (atom {:selected "Alla meddelanden"}))

(def nav-state (atom {:navigation [
  {:title "Översikt" :li.class "action_Overview" :span.class "icm icm-overview"}
  {:title "Uppdatera" :li.class "action_Update" :span.class "glyphicon glyphicon-refresh" :active true}
  {:title "Inställningar" :li.class "action_Settings" :span.class "icm icm-settings" :active true}
  {:title "Logga ut" :span.class "icm icm-logout" :active true}
]}))

;==============================================================================================================
; TOPNAVIGATOR
;==============================================================================================================
(defn logotype []
  [:div#skv-topnav-middlecol-logo {:class "col-xs-12 col-sm-3"}
    [:div {:class "skv-topnav-middlecol"}
      [:div {:class ""}
        [:div#logobutton
          [:img {:alt "Min Mynighetspost logotype" :src "https://minmyndighetspost.se/ez/minameddelanden_anvandare/images/logotyp.png"}]
        ]
      ]
    ]
  ]
)

(defn loggedin []
  [:div {:class "hidden-xs col-sm-4 col-md-5"}
    [:div#skv-topnav-userdata {:class "skv-topnav-middlecol skv-topnav-middlecol-userinfo"}
      [:div {:class "display-xs-inline-block"} "Inloggad som:"]
      [:span {:class "display-xs-inline-block username"} (str (user :name) ",\u00A0" (user :pnr))]
    ]
  ]
)

(defn nav-item [nav]
  (if (:active nav)
    [:li {:class (str "rsv-nav-responsive" (:li.class nav))}
      [:a {:title (:title nav) :href "#"}
        [:span {:class (str (:span.class nav) " rsv-center")}] (:title nav)
      ]
    ]
    []
  )
)

(defn navigation []
  [:div {:class "hidden-xs col-sm-5 col-md-4 pull-right" :style {"min-width" "298px"}}
    [:nav#skv-topnav-actions {:class "skv-topnav-middlecol skv-topnav-middlecol-userinfo pull-right" :tabIndex "1"}
      [:ul#rsvNavMenuItemsWrapperTopnav {:class "list-inline overview-mailbox-list" :aria-label "Toppmeny"}
        (map nav-item (@nav-state :navigation))
      ]
    ]
  ]
)

(defn topnavigator []
  [:header#topnavigator {:class "clearfix"}
    (logotype)
    (loggedin)
    (navigation)
  ]
)

;==============================================================================================================
; XS MENU HANDLING
;==============================================================================================================
(defn skv-topnav-submenuxs []
  [:div#skv-topnav-submenuxs {:class "row"}]
)

(defn sidebar-toggle[]
  [:div#sidebarToggleWrapper {:class "row visible-xs hidden-sm"}
    [:div#sidebarToggle {:class "col-xs-12 background-menu-light"}
      [:p#toggle {:class "pull-left"}
        [:span {:class "icm icm-menu"}]
        [:span {:class "text-menu"} "Meny"]
      ]
    ]
  ]
)

;==============================================================================================================
; SIDEBAR
;==============================================================================================================
(defn filter-all [_] true)
(defn filter-fav [m] (:favorite m))
(defn is-sender [s m] (= (:sender m) s))
(defn filter-sen [s] (partial is-sender s))

(defn unread [m] (count (filter (fn [i] (not (:read i))) m)))

(defn folder-map [g] {:title (key g) :unread (unread (val g)) :filter (filter-sen (key g))})

(defn sender-folders [mess]
  (map folder-map (group-by :sender (seq mess)))
)

(defn folders [mess]
  (concat
    (list
      {:title "Alla meddelanden" :unread (unread mess) :filter filter-all}
      {:title "Favoriter" :unread (unread (filter :favorite mess)) :filter filter-fav}
    )
    (sender-folders mess)
  )
)

(defn selected-folders [folds sel]
  (map (fn [f] (if (= (:title f) sel) (merge f {:selected true}) f)) folds)
)

(defn select-folder [sel]
  (swap! folder-state (fn [_] {:selected sel}))
  (update-main)
)

(defn selected-folder [folds sel]
  (filter (fn [f] (if (= (:title f) sel) true false)) folds)
)

(defn folder-markup [i]
  [:li {:class (if (:selected i) "current" "null") :onClick #(select-folder (:title i))}
    [:span {:class "folderitem"}
      [:a {:href "#" :title (str "Antal olästa brev" (:unread i) "st")} (str (:title i) "\u00A0")]
      (if (:unread i) [:div {:class "pull-right"} [:span {:class "badge unread_A"} (str (:unread i))]])
      (if (:unread i) [:span {:class "sr-only"} "olästa brev"])
    ]
  ]
)

(defn folderlist [f]
  [:ul#rsvNavMenuItemsWrapperSidebar {:class "folders"} (map folder-markup (selected-folders (folders (@msg-state :messages)) (@folder-state :selected))) ]
)

(defn sidebar []
  [:div#sidebar {:class "col-xs-9 col-sm-3 sidebar left-nav-background full-height" :role "navigation"}
    [:div#sidebar-contentwrapper
      [:div {:class "currentUser"}
        [:span {:class "sidebar-header"} "Vald brevlåda"]
        [:div]
        [:p [:span (str (user :name))] [:span (str ", " (user :pnr))]]
      ]
      (folderlist folders)
    ]
  ]
)

;==============================================================================================================
; TOOLS
;==============================================================================================================
(defn tools []
  [:div {:class "btn-group btn-group-sm"}
    [:button#markAllBtn {:class "btn btn-default checkAll hidden-xs" :data-target "#maillist"
                :data-textMark "Markera alla"
                :data-textUnmark "Avmarkera alla"
                :data-additionalTextOnly "#markAllDropDown"
                :data-onClick "postClickMarkAllButton"}
      [:span {:class "icon icm icm-checkbox_noselect"}]
      [:span {:class "text"} "Markera alla"]
    ]
    [:button#deleteBtn {:class "btn btn-default btn-sm hidden-xs deleteBtn disabled"} "Ta bort"]
    [:button#setAsUnreadBtn {:class "btn btn-default btn-sm hidden-xs setAsUnreadBtn disabled"} [:span {:class ""} "Markera som oläst"]]
    [:div {:class "dropdown btn-group mail-actions" :style {"display" "inline-block"}}
      [:button#sortoptions {:class "btn btn-default dropdown-toggle btn-sm" :type "button" :data-toggle "dropdown"}
        [:span {:class "hidden-xs"} "Sortera\u00A0" [:span {:class "caret"}]]
        [:span {:class "visible-xs"} "Alternativ\u00A0" [:span {:class "caret"}]]
      ]
      [:ul {:class "dropdown-menu" :role "menu" :aria-labelledby "sortoptions"}
        [:li {:role "presentation"} [:a#sortDateAscId {:role "menuitem" :href "#"} "Sortera - Nyaste först"]]
        [:li {:role "presentation"} [:a#sortDateDescId {:role "menuitem" :href "#"} "Sortera - Äldst först"]]
        [:li {:role "presentation"} [:a#sortSubjectAscId {:role "menuitem" :href "#"} "Sortera - Ämne A till Ö"]]
        [:li {:role "presentation"} [:a#sortSubjectDescId {:role "menuitem" :href "#"} "Sortera - Ämne Ö till A"]]
        [:li {:role "presentation"} [:a#sortAttentionAscId {:role "menuitem" :href "#"} "Sortera - Underadress A till Ö"]]
        [:li {:role "presentation"} [:a#sortAttentionDescId {:role "menuitem" :href "#"} "Sortera - Underadress Ö till A"]]
      ]
    ]
(comment    [:div {:class "dropdown open"}
      [:button#dropdownMenuButton {:class "btn btn-secondary dropdown-toggle" :type "button" :data-toggle "dropdown" :aria-haspopup "true" :aria-expanded "false"}
        "Dropdown button"
      ]
      [:div {:class "dropdown-menu" :aria-labelledby "dropdownMenuButton"}
        [:a {:class "dropdown-item" :href "#"} "Action"]
        [:a {:class "dropdown-item" :href "#"} "Something alse"]
        [:a {:class "dropdown-item" :href "#"} "Another action"]
      ]
    ])
  ]
)

(defn title-selected-folder []
  (:title (first (selected-folder (folders (@msg-state :messages)) (@folder-state :selected))))
)

(defn unread-selected-folder []
  (:unread (first (selected-folder (folders (@msg-state :messages)) (@folder-state :selected))))
)

(defn selected-folder-filter []
  (:filter (first (selected-folder (folders (@msg-state :messages)) (@folder-state :selected))))
)

(defn main-content []
  [:div {:class "col-xs-12 col-sm-9  xs-no-left-padding rightCol full-height no-right-padding"}
    [:div {:class "page-row mainContent full-height"}
      [:div.col-xs-12
        [:form#askToTextInMessages-handle-mailbox
          [:div#content {:class "mailbox"}]
          [:h1 (title-selected-folder) [:small " (" [:span {:class "mailboxstatus unread_A"} (unread-selected-folder)] (str " oläst" (if (> (unread-selected-folder) 1) "a)" ")"))]]
          [:div#messages {:class "row no-side-margin"}
            [:div {:class "col-xs-4 col-sm-8 toolbar mail-actions"}
              (tools)
            ]
          ]
          [:div#mailTableWrapper] ;<--- the actual mail list component are mounted here
        ]
      ]
    ]
  ]
)

;==============================================================================================================
; DATE FORMATTING
;==============================================================================================================
(def month-names
  "A vector of abbreviations for the twelve months, in order."
  ["jan" "feb" "mar" "apr" "maj" "jun" "jul" "aug" "sep" "okt" "nov" "dec"])

(defn month-name
  "Returns the abbreviation for a month in the range [1..12]."
  [month]
  (get month-names (dec month)))

(defn- parse-iso-date
  "Returns a map with keys :year, :month, and :day from the given ISO 8601 date string."
  [date]
  (zipmap [:year :month :day] (map js/parseInt (string/split date #"-0?"))))

(defn format-date
  "Converts an ISO 8601 date string to one of the format \"(D)D Mon YYYY\"."
  [date]
  (let [{:keys [day month year]} (parse-iso-date date)]
    (str day " " (month-name month) " " year))
)

;==============================================================================================================
; MESSAGE LIST FOR SELECTED FOLDER
;==============================================================================================================
(defn handle-key [m p k] (if (p m) (dissoc m k) (merge m {k true})))

(defn add-key [m k] (handle-key m (fn [_] false) k))

(defn remove-key [m k] (handle-key m (fn [_] true) k))

(defn toggle-key [m k] (handle-key m (fn [m] (k m)) k))

(defn toggle-selection
  "Returns new message list with :selection key toggeled for mesage i"
  [mess i] (map (fn [m] (if (= i (:id m)) (toggle-key m :selected) m)) mess)
)

(defn toggle-favorite
  "Returns new message list with :favorite key toggeled for mesage i"
  [mess i] (map (fn [m] (if (= i (:id m)) (toggle-key m :favorite) m)) mess)
)

(defn toggle-message-selection [i]
  (swap! msg-state (fn [_] {:messages (vec (toggle-selection (@msg-state :messages) i))}))
  (update-messages)
)

(defn toggle-message-favorite [i]
  (swap! msg-state (fn [_] {:messages (vec (toggle-favorite (@msg-state :messages) i))}))
  (update-main)
)

(defn message-markup [m]
  [(keyword (str "tr#" (:id m) (if (:read m) ".is_read" ".not_read") (if (:selected m) ".selected" ""))) {:key (:id m) :title (:title m)}
    [:td.useractions
      [:div.rsv-checkbox
        [(keyword (str "input#" (str "myBoxId_" (:id m)) ".squaredFour"))
          (merge {:type "checkbox" :name "selectedMessages" :value (:id m) :title "Markera/avmarkera  meddelandet" :onClick #(toggle-message-selection (:id m))}
                 (if (:selected m) {:checked "true"} {})
          )
        ]
        [:label {:for (str "myBoxId_" (:id m))}]
      ]
      [:div
        [:a {:class "favourite" :id (str "favoritLinkId_" (:id m)) :href "#"}
          [:span {:class (str (if (:favorite m) "active " "") "favouriteicon icm icm-flag") :onClick #(toggle-message-favorite (:id m))}]
        ]
      ]
    ]
    [:td {:class "datecolumn hidden-xs hidden-sm openmail" :title "date"} (str (format-date (:date m)))]
    [:td {:class "openmail"}
      [:div {:class "maillink-wrapper"}
        [:a.maillink {:id (str "oneMessageLinkId_" (:id m)) :href "#" :title (:title m)} (:title m)]
      ]
      [:div {:class "senderName pull-left"} (:sender m)]
      [:div {:class "pull-right"}
        (if (:att m) [:span {:class "icm icm-attachment" :title "Bilaga finns"} "\u00A0"] [])
        [:span {:class "visible-xs-inline-block visible-sm-inline-block"} (format-date (:date m))]
      ]
    ]
  ]
)

(defn folder-messages []
  (filter (selected-folder-filter) (@msg-state :messages))
)

;==============================================================================================================
; COMPONENTS AND UPDATE METHODS
;==============================================================================================================
(rum/defc body []
  [:div#accountLayout2 {:class "container-fluid wrapper"}
    (topnavigator)
    (skv-topnav-submenuxs)
    (sidebar-toggle)
    [:div#ie-body {:class "page-row row full-height"}
      [:div#pageBody {:class "full-height has-sidebar"}
        (sidebar)
        (main-content)
      ]
    ]
  ]
)

(defn update-main []
  (rum/mount (body) (. js/document (getElementById "main")))
  (update-messages)
)

(rum/defc messages []
  [:table#maillistTable {:class "table active"}
    [:tbody#maillist
      (map message-markup (sort-by :date (fn [a b] (compare b a)) (folder-messages)))
    ]
  ]
)

(defn update-messages []
  (rum/mount (messages) (. js/document (getElementById "mailTableWrapper")))
)

;==============================================================================================================
; ENTRY POINT
;==============================================================================================================
(defn init []
  (update-main)
)
;==============================================================================================================
