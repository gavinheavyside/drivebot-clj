(ns drivebot.commands.help
  (:require [drivebot.campfire :as cf]
            [robert.hooke :as rh]))

(defn help-hook [f command args]
  (if (or (= command "help") (= command ""))
    (cf/send-message "usage: drivebot <command> [args]")
    (f command args)))

(println "loading help")
(rh/add-hook #'drivebot.core/handle-command help-hook)
