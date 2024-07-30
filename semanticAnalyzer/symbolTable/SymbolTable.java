package semanticAnalyzer.symbolTable;

import lexer.WordTypeCode;
import semanticAnalyzer.symbolTable.symbolPara.FuncPara;
import semanticAnalyzer.symbolTable.symbolPara.ParamInfo;
import semanticAnalyzer.symbolTable.symbolPara.SymbolPara;
import semanticAnalyzer.symbolTable.symbolPara.VarPara;
import semanticAnalyzer.symbolTable.valueStore.ValueStore;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, SymbolPara> symbols;
    private final HashMap<String, Integer> repeat;
    private final SymbolTable pre;
    private final ArrayList<SymbolTable> posts;
    private final ValueStore valueStore;

    public SymbolTable(SymbolTable pre) {
        this.symbols = new HashMap<>();
        this.repeat = new HashMap<>();
        this.pre = pre;
        this.posts = new ArrayList<>();
        if (pre != null) {
            pre.addPost(this);
            this.valueStore = new ValueStore(pre.valueStore);
        } else {
            this.valueStore = new ValueStore(null);
        }
    }

    public boolean notDefineVar(String identName) {
        if (symbols.containsKey(identName)) {
            return false;
        } else if (pre != null) {
            return pre.notDefineVar(identName);
        }
        return true;
    }

    public boolean hasVar(String identName) {
        return symbols.containsKey(identName);
    }

    public int repeatTimes(String identName) {
        int number = repeat.getOrDefault(identName, 0);
        number += 1;
        repeat.put(identName, number);
        return number;
    }

    public void addSymbol(String name, SymbolPara para) {
        symbols.put(name, para);
    }

    public void addPost(SymbolTable symbolTable) {
        posts.add(symbolTable);
    }

    public void addVarValue(String identName, int dimen, int[] dimens, int[][] value) {
        valueStore.addVarValue(identName, dimen, dimens, value);
    }

    public int findVarValue(String identName, ArrayList<Integer> index) {
        return valueStore.findVarValue(identName, index);
    }

    public void setOutName(String identName, String outName) {
        ((VarPara) symbols.get(identName)).setOutName(outName);
    }

    public ArrayList<ParamInfo> findFuncParamInfos(String ident) {
        if (symbols.containsKey(ident)) {
            if (symbols.get(ident) instanceof FuncPara) {
                return ((FuncPara) symbols.get(ident)).getParamInfos();
            }
        } else if (pre != null) {
            return pre.findFuncParamInfos(ident);
        }
        return null;
    }

    public WordTypeCode findFuncType(String ident) {
        if (symbols.containsKey(ident)) {
            if (symbols.get(ident) instanceof FuncPara) {
                return ((FuncPara) symbols.get(ident)).getFuncType();
            }
        } else if (pre != null) {
            return pre.findFuncType(ident);
        }
        return null;
    }


    public SymbolPara findPara(String ident) {
        if (symbols.containsKey(ident)) {
            return symbols.get(ident);
        } else if (pre != null) {
            return pre.findPara(ident);
        }
        return null;
    }

    public VarPara findVarPara(String ident) {
        if (symbols.containsKey(ident)) {
            return (VarPara) symbols.get(ident);
        } else if (pre != null) {
            return pre.findVarPara(ident);
        }
        return null;
    }

    public FuncPara findFuncPara(String ident) {
        if (symbols.containsKey(ident)) {
            return (FuncPara) symbols.get(ident);
        } else if (pre != null) {
            return pre.findFuncPara(ident);
        }
        return null;
    }
}
