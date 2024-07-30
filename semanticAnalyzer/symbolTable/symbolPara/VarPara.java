package semanticAnalyzer.symbolTable.symbolPara;

public class VarPara implements SymbolPara {
    private final int dimen;
    private final int[] dimens;
    private final boolean isGlobal;
    private final boolean isConst;
    private final boolean isArrPtr;
    private String outName;

    public VarPara(int dimen, int[] dimens, boolean isGlobal, boolean isConst) {
        this.dimen = dimen;
        this.dimens = dimens;
        this.isGlobal = isGlobal;
        this.isConst = isConst;
        this.isArrPtr = false;
        this.outName = "";
    }

    public VarPara(int dimen, int[] dimens, boolean isGlobal, boolean isConst, boolean isArrPtr,
                   String outName) {
        this.dimen = dimen;
        this.dimens = dimens;
        this.isGlobal = isGlobal;
        this.isConst = isConst;
        this.isArrPtr = isArrPtr;
        this.outName = outName;
    }


    public int getDimen() {
        return dimen;
    }

    public int[] getDimens() {
        return dimens;
    }

    public boolean getIsGlobal() {
        return isGlobal;
    }


    public boolean getIsConst() {
        return isConst;
    }

    public boolean getIsArrPtr() {
        return isArrPtr;
    }

    public String getOutName() {
        return outName;
    }

    public void setOutName(String outName) {
        this.outName = outName;
    }
}
