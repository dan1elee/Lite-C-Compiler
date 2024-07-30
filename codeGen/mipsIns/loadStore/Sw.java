package codeGen.mipsIns.loadStore;

import codeGen.mipsIns.MipsIns;

public class Sw extends MipsIns {
    private final String sourceRegName;
    private final boolean isAddress;
    private final int address;
    private final String addrRegName;
    private final int delta;

    public Sw(String sourceRegName, int address) {
        this.sourceRegName = sourceRegName;
        this.isAddress = true;
        this.address = address;
        this.addrRegName = null;
        this.delta = 0;
    }

    public Sw(String sourceRegName, String addrRegName, int delta) {
        this.sourceRegName = sourceRegName;
        this.isAddress = false;
        this.address = -1;
        this.addrRegName = addrRegName;
        this.delta = delta;
    }

    @Override
    public String toString() {
        if (isAddress) {
            return "sw $" + sourceRegName + ", " + address + "($0)";
        } else {
            return "sw $" + sourceRegName + ", " + delta + "($" + addrRegName + ")";
        }
    }
}
