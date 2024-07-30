package parser.component;

import java.util.ArrayList;


public class ConstDef extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public ConstDef() {
        super(CompType.CONST_DEF);
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
