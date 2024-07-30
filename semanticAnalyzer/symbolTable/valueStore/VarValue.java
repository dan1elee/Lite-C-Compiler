package semanticAnalyzer.symbolTable.valueStore;

import java.util.ArrayList;

public class VarValue {
    private final int dimen;
    private final int[] dimens;
    private final int[][] value;

    public VarValue(int dimen, int[] dimens, int[][] value) {
        this.dimen = dimen;
        this.dimens = dimens;
        this.value = value;
    }

    public int getValueByIndex(ArrayList<Integer> index) {
        return switch (dimen) {
            case 0 -> value[0][0];
            case 1 -> value[0][index.get(0)];
            case 2 -> value[index.get(0)][index.get(1)];
            default -> 0;
            // todo后续还得考虑数组指针
        };
    }
}
