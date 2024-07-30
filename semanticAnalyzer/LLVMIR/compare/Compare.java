package semanticAnalyzer.LLVMIR.compare;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;

public class Compare extends LLVMIRIns {
    private final CmpType cmpType;
    private final String resIndex;
    private final String op1;
    private final boolean op1Value;
    private final String op2;
    private final boolean op2Value;

    public Compare(CmpType cmpType, String resIndex, String op1, boolean op1Value, String op2,
                   boolean op2Value) {
        super(LLVMIRInsType.compare);
        this.cmpType = cmpType;
        this.resIndex = resIndex;
        this.op1 = op1;
        this.op1Value = op1Value;
        this.op2 = op2;
        this.op2Value = op2Value;
    }

    public CmpType getCmpType() {
        return cmpType;
    }

    public String getResIndex() {
        return resIndex;
    }

    public String getOp1() {
        return op1;
    }

    public boolean isOp1Value() {
        return op1Value;
    }

    public String getOp2() {
        return op2;
    }

    public boolean isOp2Value() {
        return op2Value;
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
        return String.format("%%%s = icmp %s i32 %s, %s", resIndex, cmpType, op1Name, op2Name);
    }
}
