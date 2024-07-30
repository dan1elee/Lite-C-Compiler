package parser.component;

import lexer.WordDescription;

import java.util.ArrayList;

public class Btype extends SyntaxComp {
    private final WordDescription wordDescription;

    public Btype(WordDescription wordDescription) {
        super(CompType.BTYPE);
        this.wordDescription = wordDescription;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        ArrayList<SyntaxComp> ret = new ArrayList<>();
        ret.add(wordDescription);
        return ret;
    }
}
