package codeGen.mipsIns.copy;

import codeGen.mipsIns.calculate.calcuIns.Addiu;

public class Copy extends Addiu {
    public Copy(String resultRegName, String sourceRegName) {
        super(resultRegName, sourceRegName, 0);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
