(ns dire-gene-signals-input
  (:require [clojure-csv.core :as csv]
	    [clojure.contrib.string :as string]))

(defn break-into-lines [line]
  (let [ nms (string/split #"," (first line))]
   (concat (list (concat (list (first nms)) (rest line)))
	   (map list (rest nms)))))

(defn process-file [infile outfile]
  (let [input-items (binding [csv/*delimiter* \tab]
		      (csv/parse-csv (slurp infile)))
	output-items (mapcat break-into-lines input-items)]
    (spit outfile (csv/write-csv output-items))))

