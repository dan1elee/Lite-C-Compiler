package parser.component;

import java.util.ArrayList;

public class EqExp extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public EqExp() {
        super(CompType.EQ_EXP);
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
