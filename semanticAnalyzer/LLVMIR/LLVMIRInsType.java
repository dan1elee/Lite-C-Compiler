package semanticAnalyzer.LLVMIR;

public enum LLVMIRInsType {
    allocVar,
    getArrayElePtr,
    zext,
    calculate,
    compare,
    funcCall, funcDec, funcDefBegin, funcDefEnd, funcRet,
    getInt,
    br, label,
    load, store,
    putCh, putInt,
    globalVar
}
