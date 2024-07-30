package codeGen.symbolTable.symbol;

public class MipsGlobalVar implements MipsVar {
    private final int address;

    public MipsGlobalVar(int address) {
        this.address = address;
    }

    public int getAddress() {
        return address;
    }
}
