package parser.component;

import java.util.ArrayList;

public class MulExp extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public MulExp() {
        super(CompType.MUL_EXP);
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
