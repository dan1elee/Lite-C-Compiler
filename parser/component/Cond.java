package parser.component;

import java.util.ArrayList;

public class Cond extends SyntaxComp {
    private final LOrExp lOrExp;

    public Cond(LOrExp lOrExp) {
        super(CompType.COND);
        this.lOrExp = lOrExp;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        ArrayList<SyntaxComp> ret = new ArrayList<>();
        ret.add(lOrExp);
        return ret;
    }
}
