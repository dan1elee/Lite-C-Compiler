package semanticAnalyzer.LLVMIR.varDef;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class GlobalVar extends LLVMIRIns {
    private final String name;
    private final boolean isConst;
    private final int dimen;
    private final int[] dimens;
    private final int[][] value;

    public GlobalVar(String name, boolean isConst, int dimen, int[] dimens, int[][] value) {
        super(LLVMIRInsType.globalVar);
        this.name = name;
        this.isConst = isConst;
        this.dimen = dimen;
        this.dimens = dimens;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getDimen() {
        return dimen;
    }

    public int[] getDimens() {
        return dimens;
    }

    public int[][] getValue() {
        return value;
    }

    @Override
    public String toString() {
        switch (dimen) {
            case 0:
                return String.format("@%s = dso_local %s i32 %d",
                        name, (isConst) ? "constant" : "global", (value != null) ? value[0][0] : 0);
            case 1:
                StringBuilder sb = new StringBuilder(String.format("@%s = dso_local %s [%d x i32]",
                        name, (isConst) ? "constant" : "global", dimens[0]));
                sb.append(' ');
                boolean allZero = true;
                if (value != null) {
                    for (int i = 0; i < dimens[0]; i++) {
                        if (value[0][i] != 0) {
                            allZero = false;
                            break;
                        }
                    }
                }
                if (allZero) {
                    sb.append("zeroinitializer");
                } else {
                    sb.append('[');
                    for (int i = 0; i < dimens[0]; i++) {
                        if (i != 0) {
                            sb.append(", ");
                        }
                        sb.append(String.format("i32 %d", value[0][i]));
                    }
                    sb.append(']');
                }
                return sb.toString();
            case 2:
                StringBuilder sb2 = new StringBuilder(String.format("@%s = dso_local %s [%d x [%d" +
                                " x i32]]",
                        name, (isConst) ? "constant" : "global", dimens[0], dimens[1]));
                sb2.append(' ');
                //zeroinitial
                boolean allZero_all = true;
                boolean[] allZeroSub = new boolean[dimens[0]];
                if (value != null) {
                    for (int i = 0; i < dimens[0]; i++) {
                        boolean allZero_tmp = true;
                        for (int j = 0; j < dimens[1]; j++) {
                            if (value[i][j] != 0) {
                                allZero_tmp = false;
                                break;
                            }
                        }
                        allZeroSub[i] = allZero_tmp;
                        if (!allZero_tmp) {
                            allZero_all = false;
                        }
                    }
                }
                if (allZero_all) {
                    sb2.append("zeroinitializer");
                } else {
                    sb2.append('[');
                    for (int i = 0; i < dimens[0]; i++) {
                        if (i != 0) {
                            sb2.append(", ");
                        }
                        sb2.append(String.format("[%d x i32]", dimens[1]));
                        sb2.append(' ');
                        if (allZeroSub[i]) {
                            sb2.append("zeroinitializer");
                        } else {
                            sb2.append('[');
                            for (int j = 0; j < dimens[1]; j++) {
                                if (j != 0) {
                                    sb2.append(", ");
                                }
                                sb2.append(String.format("i32 %d", value[i][j]));
                            }
                            sb2.append(']');
                        }
                    }
                    sb2.append(']');
                }
                return sb2.toString();
            default:
                return "";
        }
    }
}
