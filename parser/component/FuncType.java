package parser.component;

import lexer.WordDescription;

import java.util.ArrayList;

public class FuncType extends SyntaxComp {
    private final WordDescription type;

    public FuncType(WordDescription wd) {
        super(CompType.FUNC_TYPE);
        this.type = wd;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        ArrayList<SyntaxComp> ret = new ArrayList<>();
        ret.add(type);
        return ret;
    }
}
