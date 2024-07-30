package semanticAnalyzer.LLVMIR.input;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class GetInt extends LLVMIRIns {
    private final String dest;

    public GetInt(String dest) {
        super(LLVMIRInsType.getInt);
        this.dest = dest;
    }

    public String getDest() {
        return dest;
    }

    @Override
    public String toString() {
        return String.format("%%%s = call i32 @getint()", dest);
    }
}
