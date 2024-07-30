package codeGen.mipsIns.jump;

import codeGen.mipsIns.MipsIns;

public class Jr extends MipsIns {
    @Override
    public String toString() {
        return "jr $ra";
    }
}
