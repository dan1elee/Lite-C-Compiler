package semanticAnalyzer.LLVMIR.output;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class PutCh extends LLVMIRIns {
    private final char ch;

    public PutCh(char ch) {
        super(LLVMIRInsType.putCh);
        this.ch = ch;
    }

    public char getCh() {
        return ch;
    }

    @Override
    public String toString() {
        return String.format("call void @putch(i32 %d)", (int) ch);
    }
}
