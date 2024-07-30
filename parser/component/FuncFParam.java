package parser.component;

import java.util.ArrayList;

public class FuncFParam extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public FuncFParam() {
        super(CompType.FUNC_F_PARAM);
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
