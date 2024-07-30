package semanticAnalyzer.LLVMIR.bitExt;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class Zext extends LLVMIRIns {
    private final String resIndex;
    private final String op;

    public Zext(String resIndex, String op) {
        super(LLVMIRInsType.zext);
        this.resIndex = resIndex;
        this.op = op;
    }

    public String getResIndex() {
        return resIndex;
    }

    public String getOp() {
        return op;
    }

    @Override
    public String toString() {
        String opName = "%" + op;
        return String.format("%%%s = zext i1 %s to i32", resIndex, opName);
    }
}
