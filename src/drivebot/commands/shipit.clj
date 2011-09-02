 (ns drivebot.commands.shipit
   (:require [drivebot.campfire :as cf]
             [robert.hooke :as rh]))

(def squirrel-uris ["http://shipitsquirrel.github.com/images/ship%20it%20squirrel.png"
                    "http://shipitsquirrel.github.com/images/squirrel.png"
                    "http://img.skitch.com/20100714-d6q52xajfh4cimxr3888yb77ru.jpg"])

(defn shipit-hook [f message]
  (if (re-find #"ship it" message)
      (cf/send-message (rand-nth squirrel-uris))
      (f message)))

(println "loading ship it")
(rh/add-hook #'drivebot.core/handle-phrase shipit-hook)
