package semanticAnalyzer.LLVMIR.jumpLabel;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class Br extends LLVMIRIns {
    private final boolean isNoCond;
    private final String ifTrue;
    private final String ifFalse;
    private final String cond;

    public Br(String noCondLabel) {
        super(LLVMIRInsType.br);
        this.isNoCond = true;
        this.ifTrue = noCondLabel;
        this.ifFalse = null;
        this.cond = "";
    }

    public Br(String cond, String ifTrue, String ifFalse) {
        super(LLVMIRInsType.br);
        this.isNoCond = false;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
        this.cond = cond;
    }

    public boolean isNoCond() {
        return isNoCond;
    }

    public String getCond() {
        return cond;
    }

    public String getIfTrue() {
        return ifTrue;
    }

    public String getIfFalse() {
        return ifFalse;
    }

    @Override
    public String toString() {
        if (isNoCond) {
            return String.format("br label %%%s", ifTrue);
        } else {
            return String.format("br i1 %%%s, label %%%s, label %%%s", cond, ifTrue, ifFalse);
        }
    }
}
