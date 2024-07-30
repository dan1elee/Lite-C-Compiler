package semanticAnalyzer.LLVMIR.func.funcRet;

import lexer.WordTypeCode;
import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class FuncRet extends LLVMIRIns {
    private final WordTypeCode funcType;
    private final String retIndex;
    private final boolean isValue;

    public FuncRet(WordTypeCode funcType, String retIndex, boolean isValue) {
        super(LLVMIRInsType.funcRet);
        this.funcType = funcType;
        this.retIndex = retIndex;
        this.isValue = isValue;
    }

    public FuncRet(WordTypeCode funcType, int retIndex, boolean isValue) {
        super(LLVMIRInsType.funcRet);
        this.funcType = funcType;
        this.retIndex = String.valueOf(retIndex);
        this.isValue = isValue;
    }

    public WordTypeCode getFuncType() {
        return funcType;
    }

    public String getRetIndex() {
        return retIndex;
    }

    public boolean isValue() {
        return isValue;
    }

    @Override
    public String toString() {
        if (funcType == WordTypeCode.INTTK) {
            if (isValue) {
                return "ret i32 " + retIndex;
            } else {
                return "ret i32 %" + retIndex;
            }
        } else {
            return "ret void";
        }
    }
}
