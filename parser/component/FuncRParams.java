package parser.component;

import java.util.ArrayList;

public class FuncRParams extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public FuncRParams() {
        super(CompType.FUNC_R_PARAMS);
        this.syntaxComps = new ArrayList<>();
    }

    public int getParamNumbers() {
        return (syntaxComps.size() + 1) / 2;
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
