package codeGen.symbolTable;

import codeGen.symbolTable.symbol.MipsVar;

import java.util.HashMap;

public class MipsSymbolTable {
    private final HashMap<String, MipsVar> vars;
    private final MipsSymbolTable pre;

    public MipsSymbolTable(MipsSymbolTable pre) {
        this.vars = new HashMap<>();
        this.pre = pre;
    }

    public void addSymbol(String name, MipsVar mipsVar) {
        vars.put(name, mipsVar);
    }

    public MipsVar getSymbol(String name) {
        if (vars.containsKey(name)) {
            return vars.get(name);
        }
        assert pre != null;
        return pre.getSymbol(name);
    }
}
