(ns drivebot.core
  (:require [drivebot.campfire :as cf]
            [clojure.string :as s]
            [match.core :as m]))

(defn say [args]
   (cf/send-message args))

(defn help []
  (cf/send-message "usage: drivebot <command> [args]"))

(defn handle-command [command]
  (let [cmd (str (first command))
        args (apply str (rest command))]
    (m/match [cmd]
      [("" | "help")] (help)
      ["say"] (say args)
      :else (cf/send-message "I don't know that command"))))

(defn command-for-drivebot [target]
  (= target "drivebot"))

(defn process [message]
  (if (command-for-drivebot (first message))
    (handle-command (rest message))))

(defn from-drivebot [user-id]
  (= (:id (cf/me)) user-id))

(defn message-handler [item]
  (if-let [user-id (:user_id item)]
    (if-not (from-drivebot user-id)
      (let [;;username (cf/username-for-id user-id)
            message (s/split (s/trim (:body item)) #"\s+" 3)]
        (process message)))))

(defn -main [& args]
  (cf/start message-handler))
