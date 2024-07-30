package semanticAnalyzer.LLVMIR.array;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class GetArrayElePtr extends LLVMIRIns {
    private final int dimen;
    private final int[] dimens;
    private final String[] index;
    private final boolean[] isValue;
    private final boolean isPtr;
    private final String arr;
    private final boolean isGlobal;
    private final String resName;

    public GetArrayElePtr(int dimen, int[] dimens, int[] index, String arr, boolean isGlobal,
                          String resName) {
        super(LLVMIRInsType.getArrayElePtr);
        this.dimen = dimen;
        this.dimens = dimens;
        String[] stringIndex = new String[index.length];
        for (int i = 0; i < index.length; i++) {
            stringIndex[i] = String.valueOf(index[i]);
        }
        this.index = stringIndex;
        this.isValue = null;
        this.isPtr = false;
        this.arr = arr;
        this.isGlobal = isGlobal;
        this.resName = resName;
    }

    public GetArrayElePtr(int dimen, int[] dimens, String[] index, String arr, boolean isGlobal,
                          String resName) {
        super(LLVMIRInsType.getArrayElePtr);
        this.dimen = dimen;
        this.dimens = dimens;
        this.index = index;
        this.isValue = null;
        this.isPtr = false;
        this.arr = arr;
        this.isGlobal = isGlobal;
        this.resName = resName;
    }

    public GetArrayElePtr(int dimen, int[] dimens, String[] index, boolean[] isValue,
                          boolean isPtr, String arr, boolean isGlobal, String resName) {
        super(LLVMIRInsType.getArrayElePtr);
        this.dimen = dimen;
        this.dimens = dimens;
        this.index = index;
        this.isValue = isValue;
        this.isPtr = isPtr;
        this.arr = arr;
        this.isGlobal = isGlobal;
        this.resName = resName;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public String getResName() {
        return resName;
    }

    public String getArr() {
        return arr;
    }

    public int getDimen() {
        return dimen;
    }

    public int[] getDimens() {
        return dimens;
    }

    public String[] getIndex() {
        return index;
    }

    public boolean[] getIsValue() {
        return isValue;
    }

    public boolean isPtr() {
        return isPtr;
    }

    @Override
    public String toString() {
        String arrName;
        if (isGlobal) {
            arrName = "@" + arr;
        } else {
            arrName = "%" + arr;
        }
        StringBuilder sb = new StringBuilder();
        if (!isPtr) {
            if (dimen == 1) {
                sb.append(String.format("%%%s = getelementptr [%d x i32], [%d x i32]* %s",
                        resName, dimens[0], dimens[0], arrName));
            } else {  //dimen==2
                sb.append(String.format("%%%s = getelementptr [%d x [%d x i32]], [%d x [%d x i32]]* %s",
                        resName, dimens[0], dimens[1], dimens[0], dimens[1], arrName));
            }
        } else {
            if (dimen == 1) {
                sb.append(String.format("%%%s = getelementptr i32, i32* %s", resName, arrName));
            } else { //dimen==2
                sb.append(String.format("%%%s = getelementptr [%d x i32], [%d x i32]* %s",
                        resName, dimens[1], dimens[1], arrName));
            }
        }
        if (!isPtr) {
            sb.append(", i32 0");
        }
        int len = index.length;
        for (int i = 0; i < len; i++) {
            sb.append(", i32 ");
            if (isValue != null && (!isValue[i])) {
                sb.append('%');
            }
            sb.append(index[i]);
        }
        return sb.toString();
    }
}