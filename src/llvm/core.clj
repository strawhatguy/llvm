(ns llvm.core
  (:require [clojure.string :as s])
  (:import (com.sun.jna Pointer Function)
           (com.sun.jna.ptr PointerByReference)))

;;;; TODO package llvm up with the library; this assumes mac homebrew install of llvm v10
(System/setProperty "jna.library.path" "/usr/local/opt/llvm/lib")

(defn c->clj-name [cname]
  (let [prefixless (s/replace cname #"^LLVM" "")]
    (.toLowerCase (clojure.string/replace prefixless #"([a-z0-9])([A-Z]+)"  "$1-$2"))))

(comment
  (c->clj-name "LLVMLinkInMCJIT"))

;;;; adapted from llvm-clojure-bindings
(defmacro import-llvm-func [mapping ret-type num-args]
  (let [cname (or (and (vector? mapping) (mapping 0)) mapping)
        sym   (or (and (vector? mapping) (mapping 1)) (symbol (c->clj-name cname)))]
    `(let [c-func# (com.sun.jna.Function/getFunction "LLVM" ~cname)]
       (defn ~sym [& args#]
         (assert (= ~num-args (count args#)))
         (.invoke c-func# ~ret-type (to-array args#))))))

(import-llvm-func "LLVMDoubleType" Pointer 0)
(import-llvm-func "LLVMInt32Type" Pointer 0)
(import-llvm-func "LLVMInt64Type" Pointer 0)
(import-llvm-func "LLVMConstString" Pointer 3)
(import-llvm-func "LLVMRecompileAndRelinkFunction" Pointer 2)
(import-llvm-func "LLVMConstInt" Pointer 3)
(import-llvm-func "LLVMConstReal" Pointer 2)
(import-llvm-func "LLVMBuildCall" Pointer 5)
(import-llvm-func "LLVMDisposeModule" Boolean 1)
(import-llvm-func "LLVMDisposeBuilder" Boolean 1)
(import-llvm-func "LLVMDisposeMessage" Void 1)
(import-llvm-func "LLVMBuildRet" Pointer 2)
(import-llvm-func "LLVMBuildAdd" Pointer 4)
(import-llvm-func "LLVMDumpModule" Void 1)
(import-llvm-func "LLVMWriteBitcodeToFile" Integer 2)
(import-llvm-func "LLVMCreateBuilder" Pointer 0)
(import-llvm-func "LLVMBuildGlobalString" Pointer 3)
(import-llvm-func "LLVMBuildGlobalStringPtr" Pointer 3)
(import-llvm-func "LLVMAppendBasicBlock" Pointer 2)
(import-llvm-func "LLVMPositionBuilderAtEnd" Pointer 2)
(import-llvm-func "LLVMTypeOf" Pointer 1)
(import-llvm-func "LLVMFunctionType" Pointer 4)
(import-llvm-func "LLVMAddFunction" Pointer 3)
(import-llvm-func "LLVMGetParam" Pointer 2)
(import-llvm-func "LLVMLinkInMCJIT" Pointer 0)
(import-llvm-func "LLVMCreateExecutionEngineForModule" Integer 3)
(import-llvm-func "LLVMCreateMCJITCompilerForModule" Integer 5)
(import-llvm-func "LLVMCreateGenericValueOfInt" Pointer 3)
(import-llvm-func "LLVMGenericValueToInt" Pointer 2)
(import-llvm-func "LLVMRunFunction" Pointer 4)

;;;; modules
(import-llvm-func "LLVMModuleCreateWithName" Pointer 1)
(import-llvm-func "LLVMGetFirstFunction" Pointer 1)

;;;; targets
(import-llvm-func "LLVMSetModuleDataLayout" Void 2)
(import-llvm-func "LLVMCreateTargetData" Pointer 1)
(import-llvm-func "LLVMInitializeTarget" Void 1)
(import-llvm-func "LLVMInitializeCore" Void 1)

;;;; target machine
(import-llvm-func "LLVMGetFirstTarget" Pointer 0)
(import-llvm-func "LLVMGetNextTarget" Pointer 1)
(import-llvm-func "LLVMGetTargetFromName" Pointer 1)
(import-llvm-func "LLVMGetTargetFromTriple" Boolean 3)
(import-llvm-func "LLVMGetTargetName" Pointer 1)
(import-llvm-func "LLVMGetTargetDescription" Pointer 1)
(import-llvm-func "LLVMTargetHasJIT" Boolean 1)
(import-llvm-func "LLVMTargetHasTargetMachine" Boolean 1)
(import-llvm-func "LLVMTargetHasAsmBackend" Boolean 1)
(import-llvm-func "LLVMCreateTargetMachine" Pointer 7)
(import-llvm-func "LLVMDisposeTargetMachine" Void 1)
(import-llvm-func "LLVMGetTargetMachineTarget" Pointer 1)
(import-llvm-func "LLVMGetTargetMachineTriple" Pointer 1)
(import-llvm-func "LLVMGetTargetMachineCPU" Pointer 1)
(import-llvm-func "LLVMGetTargetMachineFeatureString" Pointer 1)
(import-llvm-func "LLVMCreateTargetDataLayout" Pointer 1)
(import-llvm-func "LLVMSetTargetMachineAsmVerbosity" Void 2)
(import-llvm-func "LLVMTargetMachineEmitToFile" Boolean 5)
(import-llvm-func "LLVMTargetMachineEmitToMemoryBuffer" Boolean 5)
(import-llvm-func "LLVMGetDefaultTargetTriple" Pointer 0)
(import-llvm-func "LLVMNormalizeTargetTriple" Pointer 1)
(import-llvm-func "LLVMGetHostCPUName" Pointer 1)
(import-llvm-func "LLVMGetHostCPUFeatures" Pointer 1)

;;;; pass registry
(import-llvm-func "LLVMGetGlobalPassRegistry" Pointer 0)
