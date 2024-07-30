package parser.component;

import java.util.ArrayList;

public class Exp extends SyntaxComp {
    private final AddExp addExp;

    public Exp(AddExp addExp) {
        super(CompType.EXP);
        this.addExp = addExp;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        ArrayList<SyntaxComp> ret = new ArrayList<>();
        ret.add(addExp);
        return ret;
    }
}
