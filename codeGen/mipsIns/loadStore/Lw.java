package codeGen.mipsIns.loadStore;

import codeGen.mipsIns.MipsIns;

public class Lw extends MipsIns {
    private final String resultRegName;
    private final boolean isAddress;
    private final int address;
    private final String addrRegName;
    private final int delta;

    public Lw(String resultRegName, int address) {
        this.resultRegName = resultRegName;
        this.isAddress = true;
        this.address = address;
        this.addrRegName = null;
        this.delta = 0;
    }

    public Lw(String resultRegName, String addrRegName, int delta) {
        this.resultRegName = resultRegName;
        this.isAddress = false;
        this.address = -1;
        this.addrRegName = addrRegName;
        this.delta = delta;
    }

    @Override
    public String toString() {
        if (isAddress) {
            return "lw $" + resultRegName + ", " + address + "($0)";
        } else {
            return "lw $" + resultRegName + ", " + delta + "($" + addrRegName + ")";
        }
    }
}
