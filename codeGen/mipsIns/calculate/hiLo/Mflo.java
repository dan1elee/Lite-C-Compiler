package codeGen.mipsIns.calculate.hiLo;

import codeGen.mipsIns.MipsIns;

public class Mflo extends MipsIns {
    private final String regName;

    public Mflo(String regName) {
        this.regName = regName;
    }

    @Override
    public String toString() {
        return "mflo $" + regName;
    }
}
