package semanticAnalyzer.LLVMIR.func.funcDec;

import lexer.WordTypeCode;
import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;
import semanticAnalyzer.symbolTable.symbolPara.ParamInfo;

import java.util.ArrayList;

public class FuncDec extends LLVMIRIns {
    private final WordTypeCode funcType;
    private final String name;
    private final ArrayList<ParamInfo> paramInfos;

    public FuncDec(WordTypeCode funcType, String name, ArrayList<ParamInfo> paramInfos) {
        super(LLVMIRInsType.funcDec);
        this.funcType = funcType;
        this.name = name;
        this.paramInfos = paramInfos;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare ");
        if (funcType == WordTypeCode.INTTK) {
            sb.append("i32");
        } else {
            sb.append("void");
        }
        sb.append(" @");
        sb.append(name);
        sb.append('(');
        if (paramInfos != null) {
            for (int i = 0; i < paramInfos.size(); i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                int dimen = paramInfos.get(i).getDimen();
                switch (dimen) {
                    case 0 -> sb.append("i32");
                    case 1 -> sb.append("i32*");
                    case 2 -> {
                        sb.append('[');
                        sb.append(paramInfos.get(i).getDimen_if2());
                        sb.append(" x i32]*");
                    }
                }
            }
        }
        sb.append(')');
        return sb.toString();
    }
}
