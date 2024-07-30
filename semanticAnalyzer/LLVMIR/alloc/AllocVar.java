package semanticAnalyzer.LLVMIR.alloc;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class AllocVar extends LLVMIRIns {
    private final String resName;
    private final int dimen;
    private final int[] dimens;
    private final boolean isArrPtr;

    public AllocVar(String resName, int dimen, int[] dimens) {
        super(LLVMIRInsType.allocVar);
        this.resName = resName;
        this.dimen = dimen;
        this.dimens = dimens;
        this.isArrPtr = false;
    }

    public AllocVar(String resName, int dimen, int[] dimens, boolean isArrPtr) {
        super(LLVMIRInsType.allocVar);
        this.resName = resName;
        this.dimen = dimen;
        this.dimens = dimens;
        this.isArrPtr = isArrPtr;
    }

    public String getResName() {
        return resName;
    }

    public int getDimen() {
        return dimen;
    }

    public int[] getDimens() {
        return dimens;
    }

    public boolean isArrPtr() {
        return isArrPtr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("%");
        sb.append(resName);
        sb.append(" = alloca ");
        switch (dimen) {
            case 0 -> sb.append("i32");
            case 1 -> {
                if (isArrPtr) {
                    sb.append("i32*");
                } else {
                    sb.append('[');
                    sb.append(dimens[0]);
                    sb.append(" x i32]");
                }
            }
            case 2 -> {
                if (isArrPtr) {
                    sb.append('[');
                    sb.append(dimens[1]);
                    sb.append(" x i32]*");
                } else {
                    sb.append('[');
                    sb.append(dimens[0]);
                    sb.append(" x [");
                    sb.append(dimens[1]);
                    sb.append(" x i32]]");
                }
            }
        }
        return sb.toString();
    }
}
