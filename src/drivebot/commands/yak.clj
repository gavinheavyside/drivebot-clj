(ns drivebot.commands.yak
  (:require [drivebot.campfire :as cf]
            [robert.hooke :as rh]
            [clojure.string :as s]))

(def yak-uris ["https://github.com/hgavin/drivebot/raw/master/images/yak.jpg"
               "https://github.com/hgavin/drivebot/raw/master/images/yak2.jpg"])

(defn yak-hook [f message]
  (if (re-find #"yak shaving" (s/lower-case message))
      (cf/send-message (rand-nth yak-uris))
      (f message)))

(println "loading yak")
(rh/add-hook #'drivebot.core/handle-phrase yak-hook)
