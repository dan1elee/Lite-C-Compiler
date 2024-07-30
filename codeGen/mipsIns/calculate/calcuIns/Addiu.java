package codeGen.mipsIns.calculate.calcuIns;

import codeGen.mipsIns.MipsIns;

public class Addiu extends MipsIns {
    private final String resultRegName;
    private final String reg1Name;
    private final int value;

    public Addiu(String resultRegName, String reg1Name, int value) {
        this.resultRegName = resultRegName;
        this.reg1Name = reg1Name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "addiu $" + resultRegName + ", $" + reg1Name + ", " + value;
    }
}
