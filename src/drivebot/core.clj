(ns drivebot.core
  (:require [drivebot.campfire :as cf]
            [clojure.string :as s]
            [match.core :as m]
            [clojure.contrib.find-namespaces :as ns]))


(defn handle-command [command args]
  (m/match [command]
    [("" | "help")] (cf/send-message "usage: drivebot <command> [args]")
    :else (cf/send-message "I don't know that command")))

(defn command-for-drivebot [target]
  (= target "drivebot"))

(defn handle-phrase [message])

(defn process [message]
  (handle-phrase (s/join " " message))
  (if (command-for-drivebot (first message))
    (let [cmd  (str (second message))
          args (apply str (drop 2 message))]
      (handle-command cmd args))))

(defn from-drivebot [user-id]
  (= (:id (cf/me)) user-id))

(defn message-handler [item]
  (if-let [user-id (:user_id item)]
    (if-not (from-drivebot user-id)
      (let [;;username (cf/username-for-id user-id)
            message (s/split (s/trim (:body item)) #"\s+" 3)]
        (process message)))))

(defn drivebot-command-namespaces []
  (let [all-ns (ns/find-namespaces-on-classpath)]
    (filter #(re-find #"^drivebot\.commands" (str %)) all-ns)))

(defn load-command [arg]
  (try (require arg)
       (catch Exception e
         (println "Warning: problem requiring" arg "hook:" (.getMessage e)))))

(defn load-commands []
  (doseq [command-namespace (drivebot-command-namespaces)]
    (load-command command-namespace)))

(defn -main [& args]
  (load-commands) 
  (cf/start message-handler))
