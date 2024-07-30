package semanticAnalyzer.LLVMIR.calculate;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class Calculate extends LLVMIRIns {
    private final CalcuType calcuType;
    private final String result;
    private final String op1;
    private final boolean op1Value;
    private final String op2;
    private final boolean op2Value;


    public Calculate(CalcuType calcuType, String result, String op1,
                     boolean op1Value, String op2, boolean op2Value) {
        super(LLVMIRInsType.calculate);
        this.calcuType = calcuType;
        this.result = result;
        this.op1 = op1;
        this.op1Value = op1Value;
        this.op2 = op2;
        this.op2Value = op2Value;
    }

    public CalcuType getCalcuType() {
        return calcuType;
    }

    public boolean isOp1Value() {
        return op1Value;
    }

    public boolean isOp2Value() {
        return op2Value;
    }

    public String getOp1() {
        return op1;
    }

    public String getOp2() {
        return op2;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        String op1Name;
        if (op1Value) {
            op1Name = op1;
        } else {
            op1Name = "%" + op1;
        }
        String op2Name;
        if (op2Value) {
            op2Name = op2;
        } else {
            op2Name = "%" + op2;
        }
        return String.format("%%%s = %s i32 %s, %s",
                result, calcuType, op1Name, op2Name);
    }
}
