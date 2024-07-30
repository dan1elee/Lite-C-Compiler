package lexer;

import java.util.ArrayList;

public class Lexer {
    private StateCode state = StateCode.BLANK;
    private String str = "";


    public ArrayList<WordDescription> sentenceLexer(int line, String sentence) {
        ArrayList<WordDescription> ret = new ArrayList<>();
        int sub = 0;
        int len = sentence.length();
        if (state == StateCode.LINE_COMMENT_READING) {
            str = "";
            state = StateCode.BLANK;
        }
        while (sub < len) {
            WordDescription des = stateTransfer(sentence.charAt(sub), line);
            if (des != null) {
                ret.add(des);
            }
            sub++;
        }
        WordDescription des = lineEnd(line);
        if (des != null) {
            ret.add(des);
        }
        return ret;
    }

    public WordDescription lastLine(int line) {
        WordDescription des = null;
        if (state != StateCode.BLANK
                && state != StateCode.LINE_COMMENT_READING
                && state != StateCode.CROSSLINE_COMMENT_READING
                && state != StateCode.CROSSLINE_COMMENT_STAR_END
                && state != StateCode.CROSSLINE_COMMENT_END
                && state != StateCode.WRONG) {
            des = new WordDescription(WordTypeCode.transStateCode2WordTypeCode(state), str, line);
        } else if (state == StateCode.WRONG) {
            System.err.println("In line " + line + ", Wrong word.");
            str = "";
            state = StateCode.BLANK;
        }
        return des;
    }

    private WordDescription stateTransfer(char c, int line) {  //除特殊产物外全写
        WordDescription ret = null;
        StateCode stateBefore = state;
        boolean update = true;
        switch (stateBefore) {
            case BLANK:
            case CROSSLINE_COMMENT_END:
                if (c == '_' || isAlpha(c)) {
                    state = StateCode.IDENT_READING;
                } else if (isNum(c)) {
                    state = StateCode.NUMBER;
                } else {
                    switchWithNoCon(c);
                }
                str = String.valueOf(c);
                break;
            case IDENT_READING:
                if (c == '_' || isAlNum(c)) {
                    update = false;
                    str = str + c;
                } else {
                    switchWithNoCon(c);
                }
                if (update) {
                    WordTypeCode type = Reserve.isReserve(str);
                    if (type == null) {
                        type = WordTypeCode.IDENFR;
                    }
                    ret = new WordDescription(type, str, line);
                    str = String.valueOf(c);
                }
                break;
            case LINE_COMMENT_READING:
                // 在循环中遇到行尾清空str 状态变为blank
                break;
            case CROSSLINE_COMMENT_READING:
                if (c == '*') {
                    state = StateCode.CROSSLINE_COMMENT_STAR_END;
                }
                break;
            case CROSSLINE_COMMENT_STAR_END:
                if (c == '/') {
                    state = StateCode.CROSSLINE_COMMENT_END;
                } else if (c != '*') {
                    state = StateCode.CROSSLINE_COMMENT_READING;
                }
                break;
            case FORMATSTRING_READING:
                if (c == '"') {
                    state = StateCode.FORMATSTRING_END;
                } else if (c == '%') {
                    state = StateCode.FORMATSRING_PER;
                } else if (c == '\\') {
                    state = StateCode.FORMATSTRING_RESLASH;
                }
//                else if (!(c == 32 || c == 33 || (c >= 40 && c <= 126))) {
//                    state = StateCode.WRONG;
//                }
                str = str + c;
                break;
            case FORMATSRING_PER:
                if (c == 'd') {
                    state = StateCode.FORMATSTRING_READING;
                } else if (c == '"') {
                    state = StateCode.FORMATSTRING_END;
                } else {
                    state = StateCode.FORMATSTRING_READING;
                }
//                else {
//                    state = StateCode.WRONG;
//                }
                str = str + c;
                break;
            case FORMATSTRING_RESLASH:
                if (c == 'n') {
                    state = StateCode.FORMATSTRING_READING;
                } else if (c == '"') {
                    state = StateCode.FORMATSTRING_END;
                } else {
                    state = StateCode.FORMATSTRING_READING;
                }
//                else {
//                    state = StateCode.WRONG;
//                }
                str = str + c;
                break;
            case FORMATSTRING_END:
            case PLUS:
            case MINU:
            case MULT:
            case MOD:
            case LESS_AND_EQUAL:
            case GREAT_AND_EQUAL:
            case EQUAL2:
            case NOT_EQUAL:
            case AND_2:
            case OR_2:
            case SEMI:
            case COMMA:
            case LPARENT:
            case RPARENT:
            case LBRACK:
            case RBRACK:
            case LBRACE:
            case RBRACE:
                if (c == '_' || isAlpha(c)) {
                    state = StateCode.IDENT_READING;
                } else if (isNum(c)) {
                    state = StateCode.NUMBER;
                } else {
                    switchWithNoCon(c);
                }
                ret = new WordDescription(WordTypeCode.transStateCode2WordTypeCode(stateBefore), str, line);
                str = String.valueOf(c);
                break;
            case NUMBER:
                if (isNum(c)) {
                    update = false;
                    str = str + c;
                } else if (c == '_' || isAlpha(c)) {
                    state = StateCode.IDENT_READING;
                } else {
                    switchWithNoCon(c);
                }
                if (update) {
                    ret = new WordDescription(WordTypeCode.INTCON, str, line);
                    str = String.valueOf(c);
                }
                break;
            case DIV_COMMENTSLASH:
                if (c == '/') {
                    update = false;
                    state = StateCode.LINE_COMMENT_READING;
                    str = str + c;
                } else if (c == '*') {
                    update = false;
                    state = StateCode.CROSSLINE_COMMENT_READING;
                    str = str + c;
                } else if (c == '_' || isAlpha(c)) {
                    state = StateCode.IDENT_READING;
                } else if (isNum(c)) {
                    state = StateCode.NUMBER;
                } else {
                    switchWithNoCon(c);
                }
                if (update) {
                    ret = new WordDescription(WordTypeCode.DIV, str, line);
                    str = String.valueOf(c);
                }
                break;
            case LESS:
                if (c == '=') {
                    update = false;
                    state = StateCode.LESS_AND_EQUAL;
                    str = str + c;
                } else if (c == '_' || isAlpha(c)) {
                    state = StateCode.IDENT_READING;
                } else if (isNum(c)) {
                    state = StateCode.NUMBER;
                } else {
                    switchWithNoCon(c);
                }
                if (update) {
                    ret = new WordDescription(WordTypeCode.LSS, str, line);
                    str = String.valueOf(c);
                }
                break;
            case GREAT:
                if (c == '=') {
                    update = false;
                    state = StateCode.GREAT_AND_EQUAL;
                    str = str + c;
                } else if (c == '_' || isAlpha(c)) {
                    state = StateCode.IDENT_READING;
                } else if (isNum(c)) {
                    state = StateCode.NUMBER;
                } else {
                    switchWithNoCon(c);
                }
                if (update) {
                    ret = new WordDescription(WordTypeCode.GRE, str, line);
                    str = String.valueOf(c);
                }
                break;
            case ASSIGN_EQUAL1:
                if (c == '=') {
                    update = false;
                    state = StateCode.EQUAL2;
                    str = str + c;
                } else if (c == '_' || isAlpha(c)) {
                    state = StateCode.IDENT_READING;
                } else if (isNum(c)) {
                    state = StateCode.NUMBER;
                } else {
                    switchWithNoCon(c);
                }
                if (update) {
                    ret = new WordDescription(WordTypeCode.ASSIGN, str, line);
                    str = String.valueOf(c);
                }
                break;
            case NOT:
                if (c == '=') {
                    update = false;
                    state = StateCode.NOT_EQUAL;
                    str = str + c;
                } else if (c == '_' || isAlpha(c)) {
                    state = StateCode.IDENT_READING;
                } else if (isNum(c)) {
                    state = StateCode.NUMBER;
                } else {
                    switchWithNoCon(c);
                }
                if (update) {
                    ret = new WordDescription(WordTypeCode.NOT, str, line);
                    str = String.valueOf(c);
                }
                break;
            case AND_1:
                if (c == '&') {
                    state = StateCode.AND_2;
                }
//                else {
//                    state = StateCode.WRONG;
//                }
                str = str + c;
                break;
            case OR_1:
                if (c == '|') {
                    state = StateCode.OR_2;
                }
//                else {
//                    state = StateCode.WRONG;
//                }
                str = str + c;
                break;
//            case WRONG:
//                // System.err.println("In line " + line + ", Wrong word" + str + ".");
//                if (c == '_' || isAlpha(c)) {
//                    state = StateCode.IDENT_READING;
//                } else if (isNum(c)) {
//                    state = StateCode.NUMBER;
//                } else {
//                    switchWithNoCon(c);
//                }
//                str = String.valueOf(c);
//                break;
        }
        return ret;
    }

    private WordDescription lineEnd(int line) {
        WordDescription ret = null;
        StateCode stateBefore = state;
        switch (stateBefore) {
            case BLANK:
            case CROSSLINE_COMMENT_END:
            case AND_1:
            case OR_1:
            case LINE_COMMENT_READING:
            case CROSSLINE_COMMENT_STAR_END:
            case CROSSLINE_COMMENT_READING:
            case FORMATSTRING_READING:
            case FORMATSRING_PER:
            case FORMATSTRING_RESLASH:
                break;
            case IDENT_READING:
                WordTypeCode type = Reserve.isReserve(str);
                if (type == null) {
                    type = WordTypeCode.IDENFR;
                }
                ret = new WordDescription(type, str, line);
                state = StateCode.BLANK;
                break;
            case FORMATSTRING_END:
            case PLUS:
            case MINU:
            case MULT:
            case MOD:
            case LESS_AND_EQUAL:
            case GREAT_AND_EQUAL:
            case EQUAL2:
            case NOT_EQUAL:
            case AND_2:
            case OR_2:
            case SEMI:
            case COMMA:
            case LPARENT:
            case RPARENT:
            case LBRACK:
            case RBRACK:
            case LBRACE:
            case RBRACE:
                ret = new WordDescription(WordTypeCode.transStateCode2WordTypeCode(stateBefore), str, line);
                state = StateCode.BLANK;
                break;
            case NUMBER:
                ret = new WordDescription(WordTypeCode.INTCON, str, line);
                state = StateCode.BLANK;
                break;
            case DIV_COMMENTSLASH:
                ret = new WordDescription(WordTypeCode.DIV, str, line);
                state = StateCode.BLANK;
                break;
            case LESS:
                ret = new WordDescription(WordTypeCode.LSS, str, line);
                state = StateCode.BLANK;
                break;
            case GREAT:
                ret = new WordDescription(WordTypeCode.GRE, str, line);
                state = StateCode.BLANK;
                break;
            case ASSIGN_EQUAL1:
                ret = new WordDescription(WordTypeCode.ASSIGN, str, line);
                state = StateCode.BLANK;
                break;
            case NOT:
                ret = new WordDescription(WordTypeCode.NOT, str, line);
                state = StateCode.BLANK;
                break;
        }
        return ret;
    }

    private void switchWithNoCon(char c) {
        switch (c) {
            case ' ', '\t' -> state = StateCode.BLANK;
            case '"' -> state = StateCode.FORMATSTRING_READING;
            case '+' -> state = StateCode.PLUS;
            case '-' -> state = StateCode.MINU;
            case '*' -> state = StateCode.MULT;
            case '/' -> state = StateCode.DIV_COMMENTSLASH;
            case '%' -> state = StateCode.MOD;
            case '<' -> state = StateCode.LESS;
            case '>' -> state = StateCode.GREAT;
            case '=' -> state = StateCode.ASSIGN_EQUAL1;
            case '!' -> state = StateCode.NOT;
            case '&' -> state = StateCode.AND_1;
            case '|' -> state = StateCode.OR_1;
            case ';' -> state = StateCode.SEMI;
            case ',' -> state = StateCode.COMMA;
            case '(' -> state = StateCode.LPARENT;
            case ')' -> state = StateCode.RPARENT;
            case '[' -> state = StateCode.LBRACK;
            case ']' -> state = StateCode.RBRACK;
            case '{' -> state = StateCode.LBRACE;
            case '}' -> state = StateCode.RBRACE;
            default -> state = StateCode.WRONG;
        }
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isNum(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlNum(char c) {
        return isAlpha(c) || isNum(c);
    }
}
