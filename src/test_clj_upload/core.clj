(ns test-clj-upload.core
  (:require [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.response :refer [response]]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [ring.middleware.multipart-params.temp-file :refer [temp-file-store]]
            [ring.middleware.multipart-params.byte-array :refer [byte-array-store]]
            [hiccup.core :refer [html]]
            [hiccup.form :refer :all])
  (:import java.io.File))

(def wrap-order (atom 0))

(defn wrap-spy [handler tip]
  (fn [req]
    (do
      (swap! wrap-order inc)
      (println "order" @wrap-order ": " tip "------------------")
      (println req)
      (println "-----------------------\n")
      (handler req))))

(defn upload-file [file]
  (let [file-name (file :filename)
        size (file :size)
        tempfile (or (file :tempfile) (file :bytes))
        content-type (file :content-type)]
    (do
      (io/copy tempfile (File. (format "/Users/%s/Desktop/%s" (System/getProperty "user.name") file-name)))
      (response (str file-name "; " tempfile "; " content-type "; " size)))))

(def home
  (html [:form {:enctype "multipart/form-data" :method "POST" :action "/file"}
         (file-upload "file") [:br]
         (submit-button "submit")]))

(defroutes handler
           (GET "/" [] home)
           (POST "/file" {params :params}
             (let [file (get params "file")]
               (upload-file file))))

(def app
  (-> handler
      (wrap-spy "wrap-multipart-params")
      (wrap-multipart-params {:store (byte-array-store)})))


