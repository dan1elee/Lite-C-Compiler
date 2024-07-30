package parser.component;

import java.util.ArrayList;

public class ConstInitVal extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public ConstInitVal() {
        super(CompType.CONST_INIT_VAL);
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
