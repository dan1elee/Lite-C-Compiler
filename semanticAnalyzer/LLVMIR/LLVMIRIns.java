package semanticAnalyzer.LLVMIR;

public abstract class LLVMIRIns {
    private final LLVMIRInsType llvmirInsType;

    public LLVMIRIns(LLVMIRInsType llvmirInsType) {
        this.llvmirInsType = llvmirInsType;
    }

    public LLVMIRInsType getLlvmirInsType() {
        return llvmirInsType;
    }

    @Override
    public abstract String toString();
}
