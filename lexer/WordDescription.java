package lexer;

import parser.component.CompType;
import parser.component.LeafSyntaxComp;
import parser.component.SyntaxComp;

import java.util.ArrayList;

public class WordDescription extends LeafSyntaxComp {
    private final WordTypeCode wordTypeCode;
    private final String content;
    private final int line;

    public WordDescription(WordTypeCode wordTypeCode, String content, int line) {
        super(CompType.WORD);
        this.wordTypeCode = wordTypeCode;
        this.content = content;
        this.line = line;
    }

    @Override
    public String toString() {
        return String.format("%s %s", wordTypeCode.toString(), content);
    }

    public String toString(int pri) {
        return " ".repeat(pri * 4) + "--- " + wordTypeCode + " " + content + "\n";
    }

    public WordTypeCode getWordTypeCode() {
        return wordTypeCode;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        return new ArrayList<>();
    }

    @Override
    public WordDescription getSyntaxContent() {
        return null;
    }

    public String getContent() {
        return content;
    }

    public int getLine() {
        return line;
    }
}
