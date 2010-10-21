(ns venn
  (:require [clojure.contrib.combinatorics :as combinatorics] 
	    [clojure.set :as set]
	    [clojure-csv.core :as csv]
	    [clojure.contrib.duck-streams :as file]))

(defn read-sets-from-csv
  ([filename] (read-sets-from-csv filename false))
  ([filename row-headers]
     (let [handle-headers (if row-headers rest identity)
	   rows (csv/parse-csv (slurp filename))
	   list-of-sets (map #(disj (set (handle-headers %)) "") ;remove column headers if there are any, remove empty string from set
			     rows)
	   set-labels (if row-headers
			(map first rows)
			(map str (iterate inc 1)))]
       (map #(with-meta % {:name %2}) list-of-sets set-labels)))) ;insert the set number as metadata

(defn calc-venn-piece [sets subset] 
  (let [intersection (apply set/intersection subset)
	other-sets (set/difference (set sets) subset)
	venn-piece (apply set/difference intersection other-sets)]
    (concat (list (apply str
			 (interpose "+" (map #(:name (meta %)) subset))))
	    (list (str "UNIQUE:" (count venn-piece)))
	    venn-piece)))

(defn venn-pieces [sets]
  (map #(calc-venn-piece sets %) (filter #(> (count %) 0)
				      (combinatorics/subsets sets))))

(defn venn-diagram "generates data that can be used to construct a venn diagram. Takes an input file (which should be csv formatted, where each row is a set).  Outputs a file listing counts and data for each area of the diagram. There is no hard limit on the number of input sets." 
  [input-filename output-filename row-headers]
  (let [sets (read-sets-from-csv input-filename row-headers)]
    (file/spit output-filename (csv/write-csv (venn-pieces sets)))))
