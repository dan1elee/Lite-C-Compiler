package codeGen.mipsIns.label;

import codeGen.mipsIns.MipsIns;

public class MipsLabel extends MipsIns {
    private final String label;

    public MipsLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label + ":";
    }
}
