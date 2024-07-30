package codeGen.mipsIns.jump;

import codeGen.mipsIns.MipsIns;

public class Beqz extends MipsIns {
    private final String condRegName;
    private final String label;

    public Beqz(String condRegName, String label) {
        this.condRegName = condRegName;
        this.label = label;
    }

    @Override
    public String toString() {
        return "beq $" + condRegName + ", $0, " + label;
    }
}
