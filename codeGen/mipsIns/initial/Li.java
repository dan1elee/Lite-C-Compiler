package codeGen.mipsIns.initial;

import codeGen.mipsIns.MipsIns;

public class Li extends MipsIns {
    private final String resultRegName;
    private final int value;

    public Li(String resultRegName, int value) {
        this.resultRegName = resultRegName;
        this.value = value;
    }

    @Override
    public String toString() {
        return "li $" + resultRegName + ", " + value;
    }
}
