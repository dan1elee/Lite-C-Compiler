package parser.component;

import java.util.ArrayList;

public class CompUnit extends SyntaxComp {
    private final ArrayList<Decl> decls;
    private final ArrayList<FuncDef> funcDefs;
    private MainFuncDef mainFuncDef;

    public CompUnit() {
        super(CompType.COMP_UNIT);
        this.decls = new ArrayList<>();
        this.funcDefs = new ArrayList<>();
        this.mainFuncDef = null;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
        if (syntaxComp instanceof Decl) {
            decls.add((Decl) syntaxComp);
        } else if (syntaxComp instanceof FuncDef) {
            funcDefs.add((FuncDef) syntaxComp);
        } else if (syntaxComp instanceof MainFuncDef) {
            if (mainFuncDef != null) {
                System.err.println("Redefine of main_func");
            } else {
                mainFuncDef = (MainFuncDef) syntaxComp;
            }
        } else {
            System.err.println("Error comp_unit comp type");
        }
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        ArrayList<SyntaxComp> ret = new ArrayList<>();
        ret.addAll(decls);
        ret.addAll(funcDefs);
        ret.add(mainFuncDef);
        return ret;
    }

    public ArrayList<Decl> getDecls() {
        return decls;
    }

    public ArrayList<FuncDef> getFuncDefs() {
        return funcDefs;
    }

    public MainFuncDef getMainFuncDef() {
        return mainFuncDef;
    }

    @Override
    public String toString() {
        return toString(0);
    }
}
