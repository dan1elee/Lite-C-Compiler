package parser.component;

import java.util.ArrayList;

public class ConstExp extends SyntaxComp {
    private final AddExp addExp;

    public ConstExp(AddExp addExp) {
        super(CompType.CONST_EXP);
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
