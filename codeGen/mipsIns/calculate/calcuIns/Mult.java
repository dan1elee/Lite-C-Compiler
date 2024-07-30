package codeGen.mipsIns.calculate.calcuIns;

import codeGen.mipsIns.MipsIns;

public class Mult extends MipsIns {
    private final String reg1Name;
    private final String reg2Name;

    public Mult(String reg1Name, String reg2Name) {
        this.reg1Name = reg1Name;
        this.reg2Name = reg2Name;
    }

    @Override
    public String toString() {
        return "mult $" + reg1Name + ", $" + reg2Name;
    }
}
