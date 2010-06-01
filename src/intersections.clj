(ns intersections
  (:require [clojure.contrib.combinatorics :as combinatorics] 
	    [clojure.set :as set]
	    [com.davidsantiago.csv :as csv]
	    [clojure.contrib.duck-streams :as file]))

(defn read-sets-from-csv [filename]
  (let [list-of-sets (map #(disj (set %) "") 
			  (csv/parse-csv (slurp filename)))
	set-labels (take (count list-of-sets) (map str (iterate inc 1)))]
    (map #(with-meta % {:name %2}) list-of-sets set-labels)))

(defn filtered-subsets [set-list]
  (filter #(> (count %) 1) (combinatorics/subsets set-list)))

(defn intersections [sets]
  (map (fn [subset] 
	 (let [intr (apply set/intersection subset)]
	   (concat (map #(:name (meta %)) subset)
		   (list (str "INTERSECTION:" (count intr)))
		   intr)))
       (filtered-subsets sets)))

(defn left-circular [coll] 
  (concat (rest coll) (list (first coll))))

(defn rotations [coll]
  (take (count coll) (iterate left-circular coll)))

(defn uniques 
  "takes a collection of sets, returns a new collection of sets representing the unique items in each set (that don't appear in other sets)" 
  [coll]
  (map (fn [rotation] 
	 (let [uniq (apply set/difference rotation)] 
	   (concat (list (str "UNIQUES:" (count uniq))) uniq))) 
       (rotations coll)))

(defn print-uniques 
  [sets]
  (doseq [unique (uniques sets)]
    (println (str "Uniques in" (:name (meta unique)) ": " (seq unique) ": (Total: " (count unique) ")"))))

(defn venn-diagram [input-filename output-filename]
  (let [sets (read-sets-from-csv input-filename)]
    (file/spit output-filename (csv/write-csv (uniques sets)))
    (file/append-spit output-filename (csv/write-csv (intersections sets)))))
