package parser.component;

import java.util.ArrayList;

public class Number extends SyntaxComp {
    private final IntConst intConst;

    public Number(IntConst intConst) {
        super(CompType.NUMBER);
        this.intConst = intConst;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        ArrayList<SyntaxComp> ret = new ArrayList<>();
        ret.add(intConst);
        return ret;
    }
}
