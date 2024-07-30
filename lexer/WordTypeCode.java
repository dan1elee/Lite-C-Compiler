package lexer;

public enum WordTypeCode {
    IDENFR, INTCON, STRCON, MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK,
    ELSETK, NOT, AND, OR, FORTK, GETINTTK, PRINTFTK, RETURNTK, PLUS, MINU,
    VOIDTK, MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ, ASSIGN, SEMICN, COMMA,
    LPARENT, RPARENT, LBRACK, RBRACK, LBRACE, RBRACE;

    public static WordTypeCode transStateCode2WordTypeCode(StateCode state) {
        return switch (state) {
            case IDENT_READING -> IDENFR;
            case NUMBER -> INTCON;
            case FORMATSTRING_END -> STRCON;
            case NOT -> NOT;
            case AND_2 -> AND;
            case OR_2 -> OR;
            case PLUS -> PLUS;
            case MINU -> MINU;
            case MULT -> MULT;
            case DIV_COMMENTSLASH -> DIV;
            case MOD -> MOD;
            case LESS -> LSS;
            case LESS_AND_EQUAL -> LEQ;
            case GREAT -> GRE;
            case GREAT_AND_EQUAL -> GEQ;
            case EQUAL2 -> EQL;
            case NOT_EQUAL -> NEQ;
            case ASSIGN_EQUAL1 -> ASSIGN;
            case SEMI -> SEMICN;
            case COMMA -> COMMA;
            case LPARENT -> LPARENT;
            case RPARENT -> RPARENT;
            case LBRACK -> LBRACK;
            case RBRACK -> RBRACK;
            case LBRACE -> LBRACE;
            case RBRACE -> RBRACE;
            default -> null;
        };
    }
}
