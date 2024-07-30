package parser.component;

import java.util.ArrayList;

public class MainFuncDef extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;
    private boolean lackRParent;

    public MainFuncDef() {
        super(CompType.MAIN_FUNC_DEF);
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
