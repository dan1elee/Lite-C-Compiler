package semanticAnalyzer.LLVMIR.loadStore;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class Load extends LLVMIRIns {
    private final String source;
    private final boolean isGlobal;
    private final int ptrDepth;
    private final int dimen2;
    private final String resIndex;

    public Load(String source, boolean isGlobal, String resIndex) {
        super(LLVMIRInsType.load);
        this.source = source;
        this.isGlobal = isGlobal;
        this.ptrDepth = 0;
        this.dimen2 = -1;
        this.resIndex = resIndex;
    }

    public Load(String source, int ptrDepth, int dimen2, boolean isGlobal, String resIndex) {
        super(LLVMIRInsType.load);
        this.source = source;
        this.isGlobal = isGlobal;
        this.ptrDepth = ptrDepth;
        this.dimen2 = (ptrDepth == 2) ? dimen2 : -1;
        this.resIndex = resIndex;
    }

    public String getSource() {
        return source;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public int getPtrDepth() {
        return ptrDepth;
    }

    public int getDimen2() {
        return dimen2;
    }

    public String getResIndex() {
        return resIndex;
    }

    @Override
    public String toString() {
        String sourceName;
        if (isGlobal) {
            sourceName = "@" + source;
        } else {
            sourceName = "%" + source;
        }
        String typeString;
        if (ptrDepth == 1) {
            typeString = "i32*";
        } else if (ptrDepth == 2) {
            typeString = "[" + dimen2 + " x i32]*";
        } else { //ptrDepth == 0;
            typeString = "i32";
        }
        return String.format("%%%s = load %s, %s* %s", resIndex, typeString, typeString, sourceName);
    }
}
