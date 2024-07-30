package codeGen.mipsIns.seg.data;

import codeGen.mipsIns.MipsIns;

public class DataVar extends MipsIns {
    private final String ident;
    private final int dimen;
    private final int[] dimens;
    private final int[][] value;

    public DataVar(String ident, int dimen, int[] dimens, int[][] value) {
        this.ident = ident;
        this.dimen = dimen;
        this.dimens = dimens;
        this.value = value;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s: .word ", ident));
        switch (dimen) {
            case 0 -> {
                if (value != null) {
                    sb.append(value[0][0]);
                } else {
                    sb.append(0);
                }
            }
            case 1 -> {
                for (int i = 0; i < dimens[0]; i++) {
                    if (i != 0) {
                        sb.append(", ");
                    }
                    if (value != null) {
                        sb.append(value[0][i]);
                    } else {
                        sb.append(0);
                    }
                }
            }
            case 2 -> {
                for (int i = 0; i < dimens[0]; i++) {
                    for (int j = 0; j < dimens[1]; j++) {
                        if (i != 0 || j != 0) {
                            sb.append(", ");
                        }
                        if (value != null) {
                            sb.append(value[i][j]);
                        } else {
                            sb.append(0);
                        }
                    }
                }
            }
        }
        return sb.toString();
    }
}
