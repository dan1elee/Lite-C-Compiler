package parser.component;

import java.util.ArrayList;

public class Decl extends SyntaxComp {
    private final NormalOrConstVarDecl varDecl;

    public Decl(NormalOrConstVarDecl varDecl) {
        super(CompType.DECL);
        this.varDecl = varDecl;
    }

    @Override
    public void addComp(SyntaxComp syntaxComp) {
    }

    @Override
    public ArrayList<SyntaxComp> getComps() {
        ArrayList<SyntaxComp> ret = new ArrayList<>();
        ret.add(varDecl);
        return ret;
    }

    public NormalOrConstVarDecl getVarDecl() {
        return varDecl;
    }
}
