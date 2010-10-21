(ns venn
  (:require [clojure.contrib.combinatorics :as combinatorics] 
	    [clojure.set :as set]
	    [com.davidsantiago.csv :as csv]
	    [clojure.contrib.duck-streams :as file]))

(defn read-sets-from-csv [filename]
  (let [list-of-sets (map #(disj (set %) "") ;remove empty string from set
			  (csv/parse-csv (slurp filename)))
	set-labels (map str (iterate inc 1))]
    (map #(with-meta % {:name %2}) list-of-sets set-labels))) ;insert the set number as metadata

(defn- filtered-subsets [set-list]
  (filter #(> (count %) 1) (combinatorics/subsets set-list))) ;only want subsets where there's more than 1 item to compare

(defn intersections [sets]
  (map (fn [subset] 
	 (let [intr (apply set/intersection subset)]
	   (concat (map #(:name (meta %)) subset)
		   (list (str "INTERSECTION:" (count intr)))
		   intr)))
       (filtered-subsets sets)))

(defn- left-circular [coll] 
  (concat (rest coll) (list (first coll))))

(defn- rotations [coll]
  (take (count coll) (iterate left-circular coll)))

(defn uniques 
  "takes a collection of sets, returns a new collection of sets representing the unique items in each set (that don't appear in other sets)" 
  [coll]
  (map (fn [rotation] 
	 (let [uniq (apply set/difference rotation)] 
	   (concat (list (str "UNIQUES:" (count uniq))) uniq))) 
       (rotations coll)))

(defn venn-diagram "generates data that can be used to construct a venn diagram. Takes an input file (which should be csv formatted, where each row is a set).  Outputs a file listing counts and data for each area of the diagram. There is no hard limit on the number of input sets." 
  [input-filename output-filename]
  (let [sets (read-sets-from-csv input-filename)]
    (file/spit output-filename (csv/write-csv (uniques sets)))
    (file/append-spit output-filename (csv/write-csv (intersections sets)))))
