package parser.component;

import java.util.ArrayList;

public class FuncFParams extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public FuncFParams() {
        super(CompType.FUNC_F_PARAMS);
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
