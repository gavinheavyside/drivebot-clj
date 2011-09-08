(ns drivebot.campfire
  (:require [http.async.client :as c]
            [org.danlarkin.json :as j]
            [clojure.string :as s]))

(def auth {:user (System/getenv "campfire_api_key")
           :password "X"
           :preemptive true})

(def base-uri (System/getenv "drivebot_base_uri"))
(def streaming-base-uri "https://streaming.campfirenow.com/room/")
(def room (System/getenv "drivebot_room_id"))
(def json-headers {:content-type "application/json"})
(def escapees {\" "\\\""})

(def *client*)


(def username-for-id
  (memoize
    (fn [id]
      (let [user-uri (str base-uri "/users/" id ".json")
            resp (c/GET *client* user-uri :auth auth)]
        (println "looking up " id)
        (c/await resp)
        (let [user (j/decode (c/string resp))]
          (:name (:user user)))))))

(def me
  (memoize
    (fn []
      (let [response (c/GET *client* (str base-uri "/users/me.json") :auth auth)]
        (c/await response)
        (:user (j/decode (c/string response)))))))

(defn send-message [args]
  (let [speak-uri (str base-uri "/room/" room "/speak.json")
        body (str "{\"message\":{\"body\":\"" (s/escape args escapees) "\"}}")]
    (let [response (c/POST *client* speak-uri :auth auth :body body :headers json-headers)]
      (c/await response))))

(defn join-room []
  (let [room-join-uri (str base-uri "/room/" room "/join.json")
        response (c/POST *client* room-join-uri :auth auth)]
    (c/await response)))

(defn start [message-handler]
  (while true
    (with-open [client (c/create-client)]
      (binding [*client* client]
        (join-room)
        (let [streaming-uri (str streaming-base-uri room "/live.json")
              resp (c/stream-seq *client* :get streaming-uri
                                          :auth auth)]
          (println "(re)starting streaming")
          (doseq [item-str (c/string resp)]
            (message-handler (j/decode item-str))))))))
