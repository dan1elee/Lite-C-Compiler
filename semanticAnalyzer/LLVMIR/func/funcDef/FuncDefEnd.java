package semanticAnalyzer.LLVMIR.func.funcDef;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class FuncDefEnd extends LLVMIRIns {

    public FuncDefEnd() {
        super(LLVMIRInsType.funcDefEnd);
    }

    @Override
    public String toString() {
        return "}";
    }
}
