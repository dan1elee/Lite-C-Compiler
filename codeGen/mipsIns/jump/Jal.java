package codeGen.mipsIns.jump;

import codeGen.mipsIns.MipsIns;

public class Jal extends MipsIns {
    private final String label;

    public Jal(String label) {
        this.label = label;
    }


    @Override
    public String toString() {
        return "jal " + label;
    }
}
