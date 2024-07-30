package semanticAnalyzer.symbolTable.symbolPara;

import lexer.WordTypeCode;

import java.util.ArrayList;

public class FuncPara implements SymbolPara {
    private final WordTypeCode funcType;
    private final ArrayList<ParamInfo> paramInfos;
    private final boolean lackRParent;

    public FuncPara(WordTypeCode funcType, ArrayList<ParamInfo> paramInfos, boolean lackRParent) {
        this.funcType = funcType;
        this.paramInfos = paramInfos;
        this.lackRParent = lackRParent;
    }

    public WordTypeCode getFuncType() {
        return funcType;
    }

    public ArrayList<ParamInfo> getParamInfos() {
        return paramInfos;
    }

    public boolean isLackRParent() {
        return lackRParent;
    }
}
