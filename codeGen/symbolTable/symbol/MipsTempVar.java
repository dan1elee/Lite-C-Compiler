package codeGen.symbolTable.symbol;

public class MipsTempVar implements MipsVar {
    private final int wordNumberToFp;
    private final int dimen;
    private final boolean isPtr;
    private final boolean isPtrPtr;

    public MipsTempVar(int wordNumberToFp) {
        this.wordNumberToFp = wordNumberToFp;
        this.dimen = 0;
        this.isPtr = false;
        this.isPtrPtr = false;
    }

    public MipsTempVar(int wordNumberToFp, int dimen, boolean isPtr, boolean isPtrPtr) {
        this.wordNumberToFp = wordNumberToFp;
        this.dimen = dimen;
        this.isPtr = isPtr;
        this.isPtrPtr = isPtrPtr;
    }

    public boolean isPtr() {
        return isPtr;
    }

    public boolean isPtrPtr() {
        return isPtrPtr;
    }

    public int getDimen() {
        return dimen;
    }

    public int getWordNumberToFp() {
        return wordNumberToFp;
    }
}
