package lexer;

public class Reserve {
    public static WordTypeCode isReserve(String str) {
        return switch (str) {
            case "main" -> WordTypeCode.MAINTK;
            case "const" -> WordTypeCode.CONSTTK;
            case "int" -> WordTypeCode.INTTK;
            case "break" -> WordTypeCode.BREAKTK;
            case "continue" -> WordTypeCode.CONTINUETK;
            case "if" -> WordTypeCode.IFTK;
            case "else" -> WordTypeCode.ELSETK;
            case "for" -> WordTypeCode.FORTK;
            case "getint" -> WordTypeCode.GETINTTK;
            case "printf" -> WordTypeCode.PRINTFTK;
            case "return" -> WordTypeCode.RETURNTK;
            case "void" -> WordTypeCode.VOIDTK;
            default -> null;
        };
    }
}
