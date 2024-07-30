package parser.component;

import java.util.ArrayList;

public class Block extends SyntaxComp {
    private final ArrayList<SyntaxComp> syntaxComps;

    public Block() {
        super(CompType.BLOCK);
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
