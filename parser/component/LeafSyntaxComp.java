package parser.component;

import lexer.WordDescription;

public abstract class LeafSyntaxComp extends SyntaxComp {
    public LeafSyntaxComp(CompType type) {
        super(type);
    }

    public abstract WordDescription getSyntaxContent();
}
