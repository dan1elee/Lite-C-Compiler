package parser.component;

import lexer.WordDescription;

import java.util.ArrayList;

public class FormatString extends LeafSyntaxComp {
    private final WordDescription content;
    private final int line;

    public FormatString(WordDescription content, int line) {
        super(CompType.FORMAT_STRING);
        this.content = content;
        this.line = line;
    }

    public int getLine() {
        return line;
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

