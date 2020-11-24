(ns examples.sum
  (:require [llvm.core :as llvm])
  (:import (com.sun.jna Pointer Memory)
           (com.sun.jna.ptr PointerByReference))
  (:gen-class))

;; llvm equivalent of:
;;
;; int sum(int a, int b) {
;;     return a + b;
;; }
;;
;; from git@github.com:paulsmith/getting-started-llvm-c-api.git

(with-cleanup [[llvm/dispose-message error cpu target triple]
               [llvm/dispose-machine machine]]
  one)

;;;; dispose helper
(defmacro with-cleanup
  [names-by-clean-func & rest]
  `(let ~(reduce #(conj (conj %1 %2) (PointerByReference.))
                 [] (mapcat #(drop 1 %) names-by-clean-func))
     (try
       ~@rest
       (finally
         ~@(map (fn [func names]
                  (map (fn [name] `(~func ~name)) names))
                (map first names-by-clean-func)
                (map rest names-by-clean-func))))))

(defmacro with-message
  [names & rest]
  `(let ~(reduce #(conj (conj %1 %2) (PointerByReference.)) [] names)
     (try
       ~@rest
       (finally
         ~@(map (fn [name] `(llvm/dispose-message ~name)) names)))))

(defn compile-add-function []
  (let [mod (llvm/module-create-with-name "adder")
        params (into-array [ (llvm/int32-type) (llvm/int32-type)])
        ret-type (llvm/function-type (llvm/int32-type) params 2 0)
        sum-func (llvm/add-function mod "sum" ret-type)
        entry (llvm/append-basic-block sum-func "entry")
        builder (llvm/create-builder)]
    (llvm/position-builder-at-end builder entry)
    (let [param1 (llvm/get-param sum-func 0)
          param2 (llvm/get-param sum-func 1)
          tmp (llvm/build-add builder param1 param2 "tmp")]
      (llvm/build-ret builder tmp)
      (llvm/dispose-builder builder)
      mod)))

(defn write-out-bitcode [mod]
  (if (not (= 0 (llvm/write-bitcode-to-file mod "sum.bc")))
    (println "couldn't write out")))

(defn make-default-target []
  (with-message [error]
    (let [triple (llvm/get-default-target-triple)
          target (PointerByReference.)]
      (when (not (= 0 (llvm/get-target-from-triple triple target error)))
        (throw (.getString (.getValue error) 0)))
      target)))

(defn execute-bitcode
  ^{:doc "doesn't work, supposed to execute the compiled bitcode"}
  [mod]
  ;; (llvm/set-module-data-layout mod (llvm/create-target-data "x86_64-apple-macosx10.15.0"))
  (with-message [error]
    (let [sum-func (llvm/get-first-function mod)
          engine (PointerByReference.)]
      (println (.getString (llvm/get-default-target-triple) 0))
      (when (not (= 0 (llvm/create-mcjit-compiler-for-module engine mod 0 0 error)))
        (throw (str "failed to create exe engine, " (.getString (.getValue error) 0))))
      (let [args (into-array [(llvm/create-generic-value-of-int (llvm/int32-type) 4 0)
                              (llvm/create-generic-value-of-int (llvm/int32-type) 12 0)])
            result (llvm/run-function (.getValue engine) sum-func 2 args)]
        (println "Got: " (llvm/generic-value-to-int result 0))))))

(defn -main
  []
  (llvm/link-in-mcjit)
  (let [foo (compile-add-function)]
    (execute-bitcode foo)
    (write-out-bitcode foo)))
