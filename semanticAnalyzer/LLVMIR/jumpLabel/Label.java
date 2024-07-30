package semanticAnalyzer.LLVMIR.jumpLabel;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class Label extends LLVMIRIns {
    private final String labelName;

    public Label(String labelName) {
        super(LLVMIRInsType.label);
        this.labelName = labelName;
    }

    public String getLabelName() {
        return labelName;
    }

    @Override
    public String toString() {
        return "\n" + labelName + ":";
    }
}
