(ns drivebot.core
  (:require [http.async.client :as c]
            [org.danlarkin.json :as j]))

(def auth {:user "api_key"
           :password "X"
           :preemptive true})

(def room-uri "https://mydrivesolutionslimited.campfirenow.com/room/431641/join.json")
(def streaming-uri "https://streaming.campfirenow.com/room/431641/live.json")

(defn user-from-id [id]
  (str id))

(defn print-user-and-text [s]
  (let [item (j/decode s)
        username (user-from-id (:user_id item))
        text (:body item)]
    (println username " => " text)))

(defn join-room [client]
  (let [response (c/POST client room-uri :auth auth)]
    (c/await response)))

(defn stream-items [client]
  (let [resp (c/stream-seq client :get streaming-uri
                                  :auth auth)]
    (doseq [item-str (c/string resp)]
      (print-user-and-text item-str))))

(defn -main [& args]
  (with-open [client (c/create-client)]
    (join-room client)
    (stream-items client)))

