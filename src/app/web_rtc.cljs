(ns app.web-rtc
  (:require
    [cemerick.uri :refer [uri]]))

(def pp (.-log js/console))

(defonce rtc-conn (atom nil))

(def configuration
  {"iceServers"
   [{"url" "stun:stun.l.google.com:19302"}]})

(defn- current-uri
  []
  (-> js/window
      .-location
      uri))

(defn init-rtc-connection!
  []
  (when-not @rtc-conn
    (prn ::init-rtc-connection!)
    (let [c (js/RTCPeerConnection. (clj->js configuration))]
      (set! (.-c js/window) c)
      (set! (.-ondatachannel c) #(prn [:data-ch %]))
      (set! (.-onicecandidate c) #(prn [:ice-candidate %]))
      (set! (.-onnegotiationneeded c) #(prn [:negotiation-needed %]))

      (pp c)

      (.createOffer c
                    (fn [e]
                      (let [query {:rtc-sess-desc (.stringify js/JSON e)}]
                        (-> js/window
                            .-history
                            (.pushState (clj->js {})
                                        "New Offer"
                                        (str (assoc (current-uri)
                                                    :query
                                                    query))))))
                    #(do
                       (prn :create-offer-err)
                       (pp %))))))


;;; based on https://www.scaledrone.com/blog/webrtc-chat-tutorial/
;(defn start-web-rtc
;  [drone mode]
;  (prn (str "Starting WebRTC as " mode))
;  (let [con (js/RTCPeerConnection. (clj->js configuration))]
;    (set!
;      (.-onicecandidate con)
;      (fn [e]
;        (when-let [candidate (.candidate e)]
;          (send-signaling! drone (clj->js {:candidate candidate})))))
;
;    (case mode
;      "offerer"
;      (do
;        (set! (.-onnegotiationneeded con)
;              #(.createOffer con
;                             )))
;
;      "waiter"
;      )))

;(defn establish-signaling!
;  []
;  (let [drone (js/ScaleDrone. "LXCppaqiOVc7EpsB")
;        room-ch (chan)]
;    (.on drone "open"
;         (fn [err]
;           (if err
;             (prn err)
;             (go
;               (>! room-ch (.subscribe drone room-name))))))
;    (go
;      (let [room (<! room-ch)]
;        (.on room "open"
;             (fn [err]
;               (if err
;                 (prn err)
;                 (prn "Connected to signaling server"))))
;        (.on room "members"
;             #(let [members (js->clj %)]
;                (if (< 1 (count members))
;                  (start-web-rtc drone "waiter")
;                  (start-web-rtc drone "offerer"))))))))
