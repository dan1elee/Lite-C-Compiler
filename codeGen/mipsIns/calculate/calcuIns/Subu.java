package codeGen.mipsIns.calculate.calcuIns;

import codeGen.mipsIns.MipsIns;

public class Subu extends MipsIns {
    private final String resultRegName;
    private final String reg1Name;
    private final String reg2Name;

    public Subu(String resultRegName, String reg1Name, String reg2Name) {
        this.resultRegName = resultRegName;
        this.reg1Name = reg1Name;
        this.reg2Name = reg2Name;
    }

    @Override
    public String toString() {
        return "subu $" + resultRegName + ", $" + reg1Name + ", $" + reg2Name;
    }
}
