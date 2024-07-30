package codeGen.mipsIns.stack;

import codeGen.mipsIns.calculate.calcuIns.Addiu;

public class StackPushByte extends Addiu {
    public StackPushByte(int number) {
        super("sp", "sp", -4 * number);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
