package parser.component;

import java.util.ArrayList;

public class ForStmt extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public ForStmt() {
        super(CompType.FOR_STMT);
        this.syntaxComps = new ArrayList<>();
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
        syntaxComps.add(syntaxComp);
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        return new ArrayList<>(syntaxComps);
    }
}
