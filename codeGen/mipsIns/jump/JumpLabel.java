package codeGen.mipsIns.jump;

import codeGen.mipsIns.MipsIns;

public class JumpLabel extends MipsIns {
    private final String label;

    public JumpLabel(String label) {
        this.label = label;
    }


    @Override
    public String toString() {
        return "j " + label;
    }
}
