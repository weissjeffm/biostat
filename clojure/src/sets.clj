(ns sets
  (:require [com.davidsantiago.csv :as csv]
	    [clojure.contrib.duck-streams :as file]))

(defn read-rows [file]
  (let [rows (rest (csv/parse-csv (slurp file)))] ;skip the first row that contains column headers
    (zipmap (map first rows)
	    (map rest rows))))

(defn read-keys [file]
  (map first (csv/parse-csv (slurp file))))

(defn select [map klist]
  (dissoc (select-keys map klist) ""))

(defn write-results [sel file]
  (let [klist (keys sel)
	vlist (vals sel)
	rows (map (fn [k v] (concat (vector k) v))
		     klist
		     vlist)]
       (file/spit file (csv/write-csv rows))))

(defn process-files [data-file keys-file output-file]
  (write-results (select (read-rows data-file) (read-keys keys-file)) output-file))