package parser.component;

import java.util.ArrayList;

public class Lval extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public Lval() {
        super(CompType.LVAL);
        this.syntaxComps = new ArrayList<>();
    }

    public int getLine() {
        return ((Ident) syntaxComps.get(0)).getLine();
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
