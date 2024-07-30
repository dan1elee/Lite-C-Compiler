package parser.component;

import java.util.ArrayList;

public abstract class NormalOrConstVarDecl extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public NormalOrConstVarDecl(CompType type) {
        super(type);
        syntaxComps = new ArrayList<>();
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
