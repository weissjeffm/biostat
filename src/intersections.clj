(ns intersections
  (:use clojure.contrib.combinatorics 
	clojure.set
	com.davidsantiago.csv))

(def transpose (partial apply map vector))

(defn read-from-csv-columns [filename]
    (transpose (parse-csv (slurp filename))))

(defn mkmap [indexeditem]
  (let [index (first indexeditem) 
	item (second indexeditem) ] 
    (assoc {} :name index :value item)))

(defn labeled-subsets [list]
  (let [sets (map set list)]  
    (filter #(> (count %) 1) 
	    (subsets (map mkmap (transpose [(take (count sets) (iterate inc 1)) sets]))))))

(defn print-intersections [subsetmapslist]
  (doseq [subsetmap subsetmapslist] 
    (let [sets (map :value subsetmap)
	  intersection-items (apply intersection sets) ]
      (println (str "Intersection of " (seq (map :name subsetmap)) ": " (seq intersection-items) " (Total: " (count intersection-items) ")")))))

(defn left-circular [coll] 
  (concat (rest coll) (list (first coll))))

(defn rotations [coll]
  (take (count coll) (iterate left-circular coll)))

(defn print-uniques 
  [sets]
  (let [uniques-sets  (map #(apply difference %) (rotations sets)) ]
    (doseq [unique uniques-sets]
      (println (str "Unique: " (seq unique))))))

(defn venn-diagram [filename]
  (let [lists (read-from-csv-columns filename)]
    (print-intersections (labeled-subsets lists))
    (print-uniques (map set lists))))


