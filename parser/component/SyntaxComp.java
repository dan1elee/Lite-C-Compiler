package parser.component;

import lexer.WordDescription;

import java.util.ArrayList;

public abstract class SyntaxComp {
    private final CompType type;

    public SyntaxComp(CompType type) {
        this.type = type;
    }

    public abstract void addComp(SyntaxComp syntaxComp);

    public abstract ArrayList<SyntaxComp> getComps();

    public CompType getType() {
        return type;
    }


    public String toString(int pri) {
        String spaces = " ".repeat(pri * 4);
        StringBuilder ret = new StringBuilder(spaces + "--- " + type.toString() + "\n");
        if (!(this instanceof WordDescription)) {
            if (this instanceof LeafSyntaxComp) {
                ret.append(((LeafSyntaxComp) this).getSyntaxContent().toString(pri + 1));
            } else {
                ArrayList<SyntaxComp> comps = getComps();
                for (SyntaxComp comp : comps) {
                    ret.append(comp.toString(pri + 1));
                }
            }
        }
        return ret.toString();
    }
}
