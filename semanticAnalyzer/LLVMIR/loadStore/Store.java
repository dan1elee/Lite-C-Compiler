package semanticAnalyzer.LLVMIR.loadStore;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class Store extends LLVMIRIns {
    private final String source;
    private final boolean isValue;
    private final int ptrDepth;
    private final int dimen2;
    private final String dest;
    private final boolean destIsGlobal;

    public Store(String source, boolean isValue, String dest, boolean destIsGlobal) {
        super(LLVMIRInsType.store);
        this.source = source;
        this.isValue = isValue;
        this.ptrDepth = 0;
        this.dimen2 = -1;
        this.dest = dest;
        this.destIsGlobal = destIsGlobal;
    }

    public Store(String source, int ptrDepth, int dimen2, boolean isValue, String dest,
                 boolean destIsGlobal) {
        super(LLVMIRInsType.store);
        this.source = source;
        this.isValue = isValue;
        this.ptrDepth = ptrDepth;
        this.dimen2 = (ptrDepth == 2) ? dimen2 : -1;
        this.dest = dest;
        this.destIsGlobal = destIsGlobal;
    }

    public Store(int source, boolean isValue, String dest, boolean destIsGlobal) {
        super(LLVMIRInsType.store);
        this.source = String.valueOf(source);
        this.isValue = isValue;
        assert isValue;
        this.ptrDepth = 0;
        this.dimen2 = -1;
        this.dest = dest;
        this.destIsGlobal = destIsGlobal;
    }

    public String getSource() {
        return source;
    }

    public boolean isValue() {
        return isValue;
    }

    public String getDest() {
        return dest;
    }

    @Override
    public String toString() {
        String sourceName;
        if (isValue) {
            sourceName = source;
        } else {
            sourceName = "%" + source;
        }
        String destName;
        if (destIsGlobal) {
            destName = "@" + dest;
        } else {
            destName = "%" + dest;
        }
        String typeString;
        if (ptrDepth == 1) {
            typeString = "i32*";
        } else if (ptrDepth == 2) {
            typeString = "[" + dimen2 + " x i32]*";
        } else { //ptrDepth == 0;
            typeString = "i32";
        }
        return String.format("store %s %s, %s* %s", typeString, sourceName, typeString, destName);
    }
}
