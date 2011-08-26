(ns drivebot.core
  (:require [http.async.client :as c]
            [org.danlarkin.json :as j]
            [clojure.string :as s]))

(def auth {:user (System/getenv "campfire_api_key")
           :password "X"
           :preemptive true})

(def base-uri (System/getenv "drivebot_base_uri"))
(def room (System/getenv "drivebot_room_id"))
(def json-headers {:content-type "application/json"})

(def username-for-id
  (memoize
    (fn [client id]
      (let [user-uri (str base-uri "/users/" id ".json")
            resp (c/GET client user-uri :auth auth)]
        (println "looking up " id)
        (c/await resp)
        (let [user (j/decode (c/string resp))]
          (:name (:user user)))))))

(defn drivebot-say [client args]
  (let [speak-uri (str base-uri "/room/" room "/speak.json")
        body (str "{\"message\":{\"body\":\"" args "\"}}")]
    (println body)
    (let [response (c/POST client speak-uri :auth auth :body body :headers json-headers)]
      (c/await response)
      (println (c/string response)))))
  
(defn drivebot-process [client text]
  (if-let [action (rest (re-find #"(\w+) (.*)" text))]
    (let [command (s/lower-case (first action))
          args (second action)]
      (if (= command "say")
        (drivebot-say client args))
      (str "say!"))))

(defn action-for [client text]
  (if-let [matches (re-find #"(?i)drivebot (.*)" text)]
    (drivebot-process client (second matches))))

(defn process-message [client username text]
  (if-let [action (action-for client text)]
    (println (str text " => " action))))

(def is-drivebot
  (memoize
    (fn [client user-id]
      (let [response (c/GET client (str base-uri "/users/me.json") :auth auth)]
        (c/await response)
        (println (c/string response))
        (= (:id (:user (j/decode (c/string response)))) user-id)))))

(defn handle-message [client item]
  (if-let [user-id (:user_id item)]
    (if-not (is-drivebot client user-id)
      (let [username (username-for-id client user-id)
            text (:body item)]
        (process-message client username text)))))

(defn join-room [client]
  (let [room-join-uri (str base-uri "/room/" room "/join.json")
        response (c/POST client room-join-uri :auth auth)]
    (c/await response)))

(defn stream-items [client]
  (let [streaming-uri (str "https://streaming.campfirenow.com/room/" room "/live.json")
        resp (c/stream-seq client :get streaming-uri
                                  :auth auth)]
    (println "starting streaming")
    (doseq [item-str (c/string resp)]
      (handle-message client (j/decode item-str)))))

(defn -main [& args]
  (with-open [client (c/create-client)]
    (join-room client)
    (stream-items client)))

