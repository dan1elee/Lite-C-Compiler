package parser.component;

import lexer.WordDescription;

import java.util.ArrayList;

public class IntConst extends LeafSyntaxComp {
    private final WordDescription content;

    public IntConst(WordDescription content) {
        super(CompType.INT_CONST);
        this.content = content;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        return null;
    }

    public WordDescription getSyntaxContent() {
        return content;
    }
}
