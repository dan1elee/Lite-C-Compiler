package semanticAnalyzer.LLVMIR.output;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class PutInt extends LLVMIRIns {
    private final String index;
    private final boolean isValue;

    public PutInt(String index, boolean isValue) {
        super(LLVMIRInsType.putInt);
        this.index = index;
        this.isValue = isValue;
    }

    public String getIndex() {
        return index;
    }

    public boolean isValue() {
        return isValue;
    }

    @Override
    public String toString() {
        if (isValue) {
            return String.format("call void @putint(i32 %s)", index);
        } else {
            return String.format("call void @putint(i32 %%%s)", index);
        }
    }
}
