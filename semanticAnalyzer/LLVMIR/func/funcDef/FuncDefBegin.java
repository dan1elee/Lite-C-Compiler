package semanticAnalyzer.LLVMIR.func.funcDef;

import lexer.WordTypeCode;
import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;
import semanticAnalyzer.symbolTable.symbolPara.ParamInfo;

import java.util.ArrayList;

public class FuncDefBegin extends LLVMIRIns {
    private final WordTypeCode funcType;
    private final String name;
    private final ArrayList<ParamInfo> paramInfos;

    public FuncDefBegin(WordTypeCode funcType, String name, ArrayList<ParamInfo> paramInfos) {
        super(LLVMIRInsType.funcDefBegin);
        this.funcType = funcType;
        this.name = name;
        this.paramInfos = paramInfos;
    }

    public WordTypeCode getFuncType() {
        return funcType;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ParamInfo> getParamInfos() {
        return paramInfos;
    }

    @Override
    public String toString() {
        String type = switch (funcType) {
            case INTTK -> "i32";
            case VOIDTK -> "void";
            default -> "";
        };
        StringBuilder params_string = new StringBuilder();
        if (paramInfos != null) {
            for (int i = 0; i < paramInfos.size(); i++) {
                if (i != 0) {
                    params_string.append(", ");
                }
                int dimen = paramInfos.get(i).getDimen();
                switch (dimen) {
                    case 0 -> {
                        params_string.append("i32 %");
                        params_string.append(paramInfos.get(i).getOutName());
                    }
                    case 1 -> {
                        params_string.append("i32* %");
                        params_string.append(paramInfos.get(i).getOutName());
                    }
                    case 2 -> {
                        params_string.append('[');
                        params_string.append(paramInfos.get(i).getDimen_if2());
                        params_string.append(" x i32]* %");
                        params_string.append(paramInfos.get(i).getOutName());
                    }
                }
            }
        }
        return String.format("define dso_local %s @%s(%s){", type, name, params_string);
    }
}
