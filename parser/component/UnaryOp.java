package parser.component;

import lexer.WordDescription;

import java.util.ArrayList;

public class UnaryOp extends SyntaxComp {
    private final WordDescription type;

    public UnaryOp(WordDescription type) {
        super(CompType.UNARY_OP);
        this.type = type;
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
