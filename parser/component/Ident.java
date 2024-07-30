package parser.component;

import lexer.WordDescription;

import java.util.ArrayList;

public class Ident extends LeafSyntaxComp {
    private final WordDescription content;

    public Ident(WordDescription content) {
        super(CompType.IDENT);
        this.content = content;
    }

    public int getLine() {
        return content.getLine();
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
