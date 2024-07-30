package semanticAnalyzer.symbolTable.symbolPara;

import errorHandler.ExpValueType;

public class ParamInfo implements SymbolPara {
    private final String name;
    private final int dimen;
    private final int dimen_if2;
    private final String outName;
    private final ExpValueType expValueType;
    private final int line;

    public ParamInfo(String name, int dimen, int dimen_if2) {
        this.name = name;
        this.dimen = dimen;
        this.dimen_if2 = dimen_if2;
        this.line = -1;
        this.outName = "";
        if (dimen == 0) {
            this.expValueType = ExpValueType.VAR_INT;
        } else if (dimen == 1) {
            this.expValueType = ExpValueType.VAR_INT_ARR_1;
        } else {
            this.expValueType = ExpValueType.VAR_INT_ARR_2;
        }
    }

    public ParamInfo(String name, int dimen, int dimen_if2, String outName, int line) {
        this.name = name;
        this.dimen = dimen;
        this.dimen_if2 = dimen_if2;
        this.outName = outName;
        this.line = line;
        if (dimen == 0) {
            this.expValueType = ExpValueType.VAR_INT;
        } else if (dimen == 1) {
            this.expValueType = ExpValueType.VAR_INT_ARR_1;
        } else {
            this.expValueType = ExpValueType.VAR_INT_ARR_2;
        }
    }

    public String getName() {
        return name;
    }

    public int getDimen() {
        return dimen;
    }

    public int getDimen_if2() {
        return dimen_if2;
    }


    public int getLine() {
        return line;
    }

    public String getParamTypeString() {
        if (dimen == 0) {
            return "i32";
        } else if (dimen == 1) {
            return "i32*";
        } else {
            return "[" + dimen_if2 + " x i32]*";
        }
    }

    public int[] getDimens() {
        return switch (dimen) {
            case 0 -> new int[]{0};
            case 1 -> new int[]{-1};
            default -> new int[]{-1, dimen_if2};
        };
    }

    public String getOutName() {
        return outName;
    }

    public ExpValueType getExpValueType() {
        return expValueType;
    }
}
