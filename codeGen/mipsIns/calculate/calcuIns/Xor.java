package codeGen.mipsIns.calculate.calcuIns;

import codeGen.mipsIns.MipsIns;

public class Xor extends MipsIns {
    private final String reg1Name;
    private final String reg2Name;
    private final String resRegName;

    public Xor(String resRegName, String reg1Name, String reg2Name) {
        this.reg1Name = reg1Name;
        this.reg2Name = reg2Name;
        this.resRegName = resRegName;
    }

    @Override
    public String toString() {
        return "xor $" + resRegName + ", $" + reg1Name + ", $" + reg2Name;
    }
}
