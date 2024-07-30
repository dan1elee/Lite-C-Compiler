package parser.component;

import java.util.ArrayList;

public class FuncDef extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;
    private boolean lackRParent;

    public FuncDef() {
        super(CompType.FUNC_DEF);
        this.syntaxComps = new ArrayList<>();
        this.lackRParent = false;
    }

    public void setLackRParentTrue() {
        this.lackRParent = true;
    }

    public boolean isLackRParent() {
        return lackRParent;
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
