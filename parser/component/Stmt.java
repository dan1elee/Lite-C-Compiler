package parser.component;

import java.util.ArrayList;

public class Stmt extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public Stmt() {
        super(CompType.STMT);
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
