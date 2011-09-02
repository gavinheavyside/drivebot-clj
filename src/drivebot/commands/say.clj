 (ns drivebot.commands.say
   (:require [drivebot.campfire :as cf]
             [robert.hooke :as rh]))

(defn say-hook [f command args]
  (if (= command "say")
    (cf/send-message args)
    (f command args)))

(println "loading say")
(rh/add-hook #'drivebot.core/handle-command say-hook)
