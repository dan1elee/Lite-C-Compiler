package codeGen.mipsIns.calculate.hiLo;

import codeGen.mipsIns.MipsIns;

public class Mfhi extends MipsIns {
    private final String regName;

    public Mfhi(String regName) {
        this.regName = regName;
    }

    @Override
    public String toString() {
        return "mfhi $" + regName;
    }
}
