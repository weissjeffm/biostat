;; (require :fset)

(asdf:oos 'asdf:load-op :fset)
(defpackage #:biostat (:use #:common-lisp))

(in-package :biostat)

(defun mytest ()
  (fset:set 1 2 3))
