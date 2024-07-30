package semanticAnalyzer.LLVMIR.func.funcCall;

import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.LLVMIRInsType;
import semanticAnalyzer.symbolTable.symbolPara.ParamInfo;

import java.util.ArrayList;

public class FuncCall extends LLVMIRIns {
    private final String funcName;
    private final ArrayList<ParamInfo> paramInfos;
    private final ArrayList<String> paramIndex;
    private final ArrayList<Boolean> paramIsValue;
    private final String resIndex;
    private final boolean isVoid;

    public FuncCall(String funcName, ArrayList<ParamInfo> paramInfos,
                    ArrayList<String> paramIndex, ArrayList<Boolean> paramIsValue,
                    String resIndex, boolean isVoid) {
        super(LLVMIRInsType.funcCall);
        this.funcName = funcName;
        this.paramInfos = paramInfos;
        this.paramIndex = paramIndex;
        this.paramIsValue = paramIsValue;
        this.resIndex = resIndex;
        this.isVoid = isVoid;
    }

    public String getFuncName() {
        return funcName;
    }

    public int getParamInfosNumber() {
        return paramInfos.size();
    }

    public ArrayList<String> getParamIndex() {
        return paramIndex;
    }

    public ArrayList<Boolean> getParamIsValue() {
        return paramIsValue;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public String getResIndex() {
        return resIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isVoid) {
            sb.append("call void @");
        } else {
            sb.append('%');
            sb.append(resIndex);
            sb.append(" = call i32 @");
        }
        sb.append(funcName);
        sb.append('(');
        assert paramInfos.size() == paramIndex.size();
        assert paramIndex.size() == paramIsValue.size();
        int len = paramInfos.size();
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(paramInfos.get(i).getParamTypeString());
            sb.append(' ');
            if (!paramIsValue.get(i)) {
                sb.append('%');
            }
            sb.append(paramIndex.get(i));
        }
        sb.append(')');
        return sb.toString();
    }
}
