package parser;

import errorHandler.ErrorDescription;
import errorHandler.ErrorType;
import lexer.WordDescription;
import lexer.WordTypeCode;
import parser.component.AddExp;
import parser.component.Block;
import parser.component.BlockItem;
import parser.component.Btype;
import parser.component.CompUnit;
import parser.component.Cond;
import parser.component.ConstDecl;
import parser.component.ConstDef;
import parser.component.ConstExp;
import parser.component.ConstInitVal;
import parser.component.Decl;
import parser.component.EqExp;
import parser.component.Exp;
import parser.component.ForStmt;
import parser.component.FormatString;
import parser.component.FuncDef;
import parser.component.FuncFParam;
import parser.component.FuncFParams;
import parser.component.FuncRParams;
import parser.component.FuncType;
import parser.component.Ident;
import parser.component.InitVal;
import parser.component.IntConst;
import parser.component.LAndExp;
import parser.component.LOrExp;
import parser.component.Lval;
import parser.component.MainFuncDef;
import parser.component.MulExp;
import parser.component.Number;
import parser.component.PrimaryExp;
import parser.component.RelExp;
import parser.component.Stmt;
import parser.component.UnaryExp;
import parser.component.UnaryOp;
import parser.component.VarDecl;
import parser.component.VarDef;

import java.util.ArrayList;

public class Parser {
    private final ArrayList<WordDescription> descriptions;
    private int index;
    private final ArrayList<ErrorDescription> errorDescriptions;

    public Parser(ArrayList<WordDescription> descriptions,
                  ArrayList<ErrorDescription> errorDescriptions) {
        this.descriptions = descriptions;
        this.errorDescriptions = errorDescriptions;
        this.index = 0;
    }

    public CompUnit parse() {
        return parseCompUnit();
    }

    private CompUnit parseCompUnit() {
        CompUnit compUnit = new CompUnit();
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.CONSTTK
                || (descriptions.get(index).getWordTypeCode() == WordTypeCode.INTTK
                && descriptions.get(index + 1).getWordTypeCode() == WordTypeCode.IDENFR
                && descriptions.get(index + 2).getWordTypeCode() != WordTypeCode.LPARENT)) {
            compUnit.addComp(parseDecl());
        }
        while ((descriptions.get(index).getWordTypeCode() == WordTypeCode.VOIDTK
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.INTTK)
                && descriptions.get(index + 1).getWordTypeCode() == WordTypeCode.IDENFR) {
            compUnit.addComp(parseFuncDef());
        }
        compUnit.addComp(parseMainFuncDef());
        return compUnit;
    }

    private Decl parseDecl() {   //无需printParse
        Decl decl;
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.CONSTTK) {
            decl = new Decl(parseConstDecl());
        } else {
            decl = new Decl(parseVarDecl());
        }
        return decl;
    }

    private ConstDecl parseConstDecl() {
        ConstDecl constDecl = new ConstDecl();
        constDecl.addComp(descriptions.get(index));
        index++;
        constDecl.addComp(parseBType());
        constDecl.addComp(parseConstDef());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.COMMA) {
            constDecl.addComp(descriptions.get(index));
            index++;
            constDecl.addComp(parseConstDef());
        }
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
            constDecl.addComp(descriptions.get(index));
            index++;
        } else {
            int line = descriptions.get(index - 1).getLine();
            constDecl.addComp(new WordDescription(WordTypeCode.SEMICN, ";", line));
            errorDescriptions.add(new ErrorDescription(line, ErrorType.i));
        }
        return constDecl;
    }

    private Btype parseBType() {  //无需printParse
        Btype btype = new Btype(descriptions.get(index));
        index++;
        return btype;
    }

    private ConstDef parseConstDef() {
        ConstDef constDef = new ConstDef();
        Ident ident = parseIdent();
        constDef.addComp(ident);
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACK) {
            constDef.addComp(descriptions.get(index));
            index++;
            constDef.addComp(parseConstExp());
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RBRACK) {
                constDef.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                constDef.addComp(new WordDescription(WordTypeCode.RBRACK, "]", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.k));
            }
        }
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.ASSIGN) {
            constDef.addComp(descriptions.get(index));
            index++;
        }   //错误预留
        constDef.addComp(parseConstInitVal());
        return constDef;
    }

    private ConstInitVal parseConstInitVal() {
        ConstInitVal constInitVal = new ConstInitVal();
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACE) {
            constInitVal.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() != WordTypeCode.RBRACE) {
                constInitVal.addComp(parseConstInitVal());
                while (descriptions.get(index).getWordTypeCode() == WordTypeCode.COMMA) {
                    constInitVal.addComp(descriptions.get(index));
                    index++;
                    constInitVal.addComp(parseConstInitVal());
                }
            }
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RBRACE) {
                constInitVal.addComp(descriptions.get(index));
                index++;
            }
        } else {
            constInitVal.addComp(parseConstExp());
        }
        return constInitVal;
    }

    private VarDecl parseVarDecl() {
        VarDecl varDecl = new VarDecl();
        varDecl.addComp(parseBType());
        varDecl.addComp(parseVarDef());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.COMMA) {
            varDecl.addComp(descriptions.get(index));
            index++;
            varDecl.addComp(parseVarDef());
        }
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
            varDecl.addComp(descriptions.get(index));
            index++;
        } else {
            int line = descriptions.get(index - 1).getLine();
            varDecl.addComp(new WordDescription(WordTypeCode.SEMICN, ";", line));
            errorDescriptions.add(new ErrorDescription(line, ErrorType.i));
        }
        return varDecl;
    }

    private VarDef parseVarDef() {
        VarDef varDef = new VarDef();
        varDef.addComp(parseIdent());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACK) {
            varDef.addComp(descriptions.get(index));
            index++;
            varDef.addComp(parseConstExp());
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RBRACK) {
                varDef.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                varDef.addComp(new WordDescription(WordTypeCode.RBRACK, "]", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.k));
            }
        }
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.ASSIGN) {
            varDef.addComp(descriptions.get(index));
            index++;
            varDef.addComp(parseInitVal());
        }
        return varDef;
    }

    private InitVal parseInitVal() {
        InitVal initVal = new InitVal();
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACE) {
            initVal.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() != WordTypeCode.RBRACE) {
                initVal.addComp(parseInitVal());
                while (descriptions.get(index).getWordTypeCode() == WordTypeCode.COMMA) {
                    initVal.addComp(descriptions.get(index));
                    index++;
                    initVal.addComp(parseInitVal());
                }
            }
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RBRACE) {
                initVal.addComp(descriptions.get(index));
                index++;
            }
        } else {
            initVal.addComp(parseExp());
        }
        return initVal;
    }

    private FuncDef parseFuncDef() {
        FuncDef funcDef = new FuncDef();
        funcDef.addComp(parseFuncType());
        funcDef.addComp(parseIdent());
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LPARENT) {
            funcDef.addComp(descriptions.get(index));
            index++;
        }   // 错误预留
        if (descriptions.get(index).getWordTypeCode() != WordTypeCode.RPARENT
                && descriptions.get(index).getWordTypeCode() != WordTypeCode.LBRACE) {
            funcDef.addComp(parseFuncFParams());
        }
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RPARENT) {
            funcDef.addComp(descriptions.get(index));
            index++;
        } else {
            int line = descriptions.get(index - 1).getLine();
            funcDef.addComp(new WordDescription(WordTypeCode.RPARENT, ")", line));
            funcDef.setLackRParentTrue();
            errorDescriptions.add(new ErrorDescription(line, ErrorType.j));
        }
        funcDef.addComp(parseBlock());
        return funcDef;
    }

    private MainFuncDef parseMainFuncDef() {
        MainFuncDef mainFuncDef = new MainFuncDef();
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.INTTK) {
            mainFuncDef.addComp(descriptions.get(index));
            index++;
        }   //错误预留
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.MAINTK) {
            mainFuncDef.addComp(descriptions.get(index));
            index++;
        }   //错误预留
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LPARENT) {
            mainFuncDef.addComp(descriptions.get(index));
            index++;
        }   //错误预留
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RPARENT) {
            mainFuncDef.addComp(descriptions.get(index));
            index++;
        } else {
            int line = descriptions.get(index - 1).getLine();
            mainFuncDef.addComp(new WordDescription(WordTypeCode.RPARENT, ")", line));
            mainFuncDef.setLackRParentTrue();
            errorDescriptions.add(new ErrorDescription(line, ErrorType.j));
        }
        mainFuncDef.addComp(parseBlock());
        return mainFuncDef;
    }

    private FuncType parseFuncType() {
        FuncType funcType = null;
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.VOIDTK
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.INTTK) {
            funcType = new FuncType(descriptions.get(index));
            index++;
        }
        return funcType;
    }

    private FuncFParams parseFuncFParams() {
        FuncFParams funcFParams = new FuncFParams();
        funcFParams.addComp(parseFuncFParam());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.COMMA) {
            funcFParams.addComp(descriptions.get(index));
            index++;
            funcFParams.addComp(parseFuncFParam());
        }
        return funcFParams;
    }

    private FuncFParam parseFuncFParam() {
        FuncFParam funcFParam = new FuncFParam();
        funcFParam.addComp(parseBType());
        funcFParam.addComp(parseIdent());
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACK) {
            funcFParam.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RBRACK) {
                funcFParam.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                funcFParam.addComp(new WordDescription(WordTypeCode.RBRACK, "]", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.k));
            }
            while (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACK) {
                funcFParam.addComp(descriptions.get(index));
                index++;
                funcFParam.addComp(parseConstExp());
                if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RBRACK) {
                    funcFParam.addComp(descriptions.get(index));
                    index++;
                } else {
                    int line = descriptions.get(index - 1).getLine();
                    funcFParam.addComp(new WordDescription(WordTypeCode.RBRACK, "]", line));
                    errorDescriptions.add(new ErrorDescription(line, ErrorType.k));
                }
            }
        }
        return funcFParam;
    }

    private Block parseBlock() {
        Block block = new Block();
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACE) {
            block.addComp(descriptions.get(index));
            index++;
            while (descriptions.get(index).getWordTypeCode() != WordTypeCode.RBRACE) {
                block.addComp(parseBlockItem());
            }
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RBRACE) {
                block.addComp(descriptions.get(index));
                index++;
            }
        }
        return block;
    }

    private BlockItem parseBlockItem() {  //无需printParse
        BlockItem blockItem = new BlockItem();
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.CONSTTK
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.INTTK) {
            blockItem.addComp(parseDecl());
        } else {
            blockItem.addComp(parseStmt());
        }
        return blockItem;
    }

    private Stmt parseStmt() { //todo
        Stmt stmt = new Stmt();
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACE) {  //3
            stmt.addComp(parseBlock());
        } else if (descriptions.get(index).getWordTypeCode() == WordTypeCode.IFTK) { //4
            stmt.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LPARENT) {
                stmt.addComp(descriptions.get(index));
                index++;
            }
            stmt.addComp(parseCond());
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RPARENT) {
                stmt.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                stmt.addComp(new WordDescription(WordTypeCode.RPARENT, ")", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.j));
            }
            stmt.addComp(parseStmt());
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.ELSETK) {
                stmt.addComp(descriptions.get(index));
                index++;
                stmt.addComp(parseStmt());
            }
        } else if (descriptions.get(index).getWordTypeCode() == WordTypeCode.FORTK) { //5
            stmt.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LPARENT) {
                stmt.addComp(descriptions.get(index));
                index++;
            }
            if (descriptions.get(index).getWordTypeCode() != WordTypeCode.SEMICN) {
                stmt.addComp(parseForStmt());
            }
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
                stmt.addComp(descriptions.get(index));
                index++;
            }
            if (descriptions.get(index).getWordTypeCode() != WordTypeCode.SEMICN) {
                stmt.addComp(parseCond());
            }
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
                stmt.addComp(descriptions.get(index));
                index++;
            }
            if (descriptions.get(index).getWordTypeCode() != WordTypeCode.RPARENT) {
                stmt.addComp(parseForStmt());
            }
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RPARENT) {
                stmt.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                stmt.addComp(new WordDescription(WordTypeCode.RPARENT, ")", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.j));
            }
            stmt.addComp(parseStmt());
        } else if (descriptions.get(index).getWordTypeCode() == WordTypeCode.BREAKTK) {//6
            stmt.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
                stmt.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                stmt.addComp(new WordDescription(WordTypeCode.SEMICN, ";", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.i));
            }
        } else if (descriptions.get(index).getWordTypeCode() == WordTypeCode.CONTINUETK) { //6
            stmt.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
                stmt.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                stmt.addComp(new WordDescription(WordTypeCode.SEMICN, ";", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.i));
            }
        } else if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RETURNTK) { //7
            stmt.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() != WordTypeCode.SEMICN) {
                stmt.addComp(parseExp());
            }
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
                stmt.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                stmt.addComp(new WordDescription(WordTypeCode.SEMICN, ";", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.i));
            }
        } else if (descriptions.get(index).getWordTypeCode() == WordTypeCode.PRINTFTK) { //9
            stmt.addComp(descriptions.get(index));
            index++;
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LPARENT) {
                stmt.addComp(descriptions.get(index));
                index++;
                stmt.addComp(parseFormatString());
                while (descriptions.get(index).getWordTypeCode() == WordTypeCode.COMMA) {
                    stmt.addComp(descriptions.get(index));
                    index++;
                    stmt.addComp(parseExp());
                }
                if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RPARENT) {
                    stmt.addComp(descriptions.get(index));
                    index++;
                } else {
                    int line = descriptions.get(index - 1).getLine();
                    stmt.addComp(new WordDescription(WordTypeCode.RPARENT, ")", line));
                    errorDescriptions.add(new ErrorDescription(line, ErrorType.j));
                }
                if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
                    stmt.addComp(descriptions.get(index));
                    index++;
                } else {
                    int line = descriptions.get(index - 1).getLine();
                    stmt.addComp(new WordDescription(WordTypeCode.SEMICN, ";", line));
                    errorDescriptions.add(new ErrorDescription(line, ErrorType.i));
                }
            }
        } else {     // 1 2 8
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) { //2 无exp
                stmt.addComp(descriptions.get(index));
                index++;
            } else {
                int tmp = index + 1;
                boolean flag = false;
                while (descriptions.get(tmp).getWordTypeCode() != WordTypeCode.SEMICN
                        && descriptions.get(tmp).getLine() == descriptions.get(tmp - 1).getLine()) {
                    if (descriptions.get(tmp).getWordTypeCode() == WordTypeCode.ASSIGN) {
                        flag = true;
                        break;
                    }
                    tmp++;
                }
                if (flag) {
                    stmt.addComp(parseLVal());
                    if (descriptions.get(index).getWordTypeCode() == WordTypeCode.ASSIGN) {
                        stmt.addComp(descriptions.get(index));
                        index++;
                    }
                    if (descriptions.get(index).getWordTypeCode() == WordTypeCode.GETINTTK) {    //8
                        stmt.addComp(descriptions.get(index));
                        index++;
                        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LPARENT) {
                            stmt.addComp(descriptions.get(index));
                            index++;
                        }
                        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RPARENT) {
                            stmt.addComp(descriptions.get(index));
                            index++;
                        } else {
                            int line = descriptions.get(index - 1).getLine();
                            stmt.addComp(new WordDescription(WordTypeCode.RPARENT, ")", line));
                            errorDescriptions.add(new ErrorDescription(line, ErrorType.j));
                        }
                    } else {    //1
                        stmt.addComp(parseExp());
                    }
                } else {    //2 有exp
                    stmt.addComp(parseExp());
                }
                if (descriptions.get(index).getWordTypeCode() == WordTypeCode.SEMICN) {
                    stmt.addComp(descriptions.get(index));
                    index++;
                } else {
                    int line = descriptions.get(index - 1).getLine();
                    stmt.addComp(new WordDescription(WordTypeCode.SEMICN, ";", line));
                    errorDescriptions.add(new ErrorDescription(line, ErrorType.i));
                }
            }
        }
        return stmt;
    }

    private ForStmt parseForStmt() {
        ForStmt forStmt = new ForStmt();
        forStmt.addComp(parseLVal());
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.ASSIGN) {
            forStmt.addComp(descriptions.get(index));
            index++;
        }
        forStmt.addComp(parseExp());
        return forStmt;
    }

    private Exp parseExp() {
        return new Exp(parseAddExp());
    }

    private Cond parseCond() {
        return new Cond(parseLOrExp());
    }

    private Lval parseLVal() {
        Lval lval = new Lval();
        lval.addComp(parseIdent());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.LBRACK) {
            lval.addComp(descriptions.get(index));
            index++;
            lval.addComp(parseExp());
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RBRACK) {
                lval.addComp(descriptions.get(index));
                index++;
            } else {
                int line = descriptions.get(index - 1).getLine();
                lval.addComp(new WordDescription(WordTypeCode.RBRACK, "]", line));
                errorDescriptions.add(new ErrorDescription(line, ErrorType.k));
            }
        }
        return lval;
    }

    private PrimaryExp parsePrimaryExp() {
        PrimaryExp primaryExp = new PrimaryExp();
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LPARENT) {
            primaryExp.addComp(descriptions.get(index));
            index++;
            primaryExp.addComp(parseExp());
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RPARENT) {
                primaryExp.addComp(descriptions.get(index));
                index++;
            }   //错误处理预留
        } else if (descriptions.get(index).getWordTypeCode() == WordTypeCode.IDENFR) {
            primaryExp.addComp(parseLVal());
        } else {
            primaryExp.addComp(parseNumber());
        }
        return primaryExp;
    }

    private Number parseNumber() {
        return new Number(parseIntConst());
    }

    private UnaryExp parseUnaryExp() {
        UnaryExp unaryExp = new UnaryExp();
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.PLUS
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.MINU
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.NOT) {
            unaryExp.addComp(parseUnaryOp());
            unaryExp.addComp(parseUnaryExp());
        } else if (descriptions.get(index).getWordTypeCode() == WordTypeCode.IDENFR
                && descriptions.get(index + 1).getWordTypeCode() == WordTypeCode.LPARENT) {
            unaryExp.addComp(parseIdent());
            if (descriptions.get(index).getWordTypeCode() == WordTypeCode.LPARENT) {
                unaryExp.addComp(descriptions.get(index));
                index++;
                if (descriptions.get(index).getWordTypeCode() != WordTypeCode.RPARENT
                        && descriptions.get(index).getWordTypeCode() != WordTypeCode.SEMICN
                        && descriptions.get(index).getWordTypeCode() != WordTypeCode.COMMA) {
                    unaryExp.addComp(parseFuncRParams());
                }
                if (descriptions.get(index).getWordTypeCode() == WordTypeCode.RPARENT) {
                    unaryExp.addComp(descriptions.get(index));
                    index++;
                } else {
                    int line = descriptions.get(index - 1).getLine();
                    unaryExp.addComp(new WordDescription(WordTypeCode.RPARENT, ")", line));
                    errorDescriptions.add(new ErrorDescription(line, ErrorType.j));
                }
            }
        } else {
            unaryExp.addComp(parsePrimaryExp());
        }
        return unaryExp;
    }

    private UnaryOp parseUnaryOp() {
        UnaryOp unaryOp = null;
        WordTypeCode type = descriptions.get(index).getWordTypeCode();
        if (type == WordTypeCode.PLUS || type == WordTypeCode.MINU || type == WordTypeCode.NOT) {
            unaryOp = new UnaryOp(descriptions.get(index));
            index++;
        }   //错误处理预留
        return unaryOp;
    }

    private FuncRParams parseFuncRParams() {
        FuncRParams funcRParams = new FuncRParams();
        funcRParams.addComp(parseExp());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.COMMA) {
            funcRParams.addComp(descriptions.get(index));
            index++;
            funcRParams.addComp(parseExp());
        }
        return funcRParams;
    }

    private MulExp parseMulExp() {
        MulExp mulExp = new MulExp();
        mulExp.addComp(parseUnaryExp());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.MULT
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.DIV
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.MOD) {
            mulExp.addComp(descriptions.get(index));
            index++;
            mulExp.addComp(parseUnaryExp());
        }
        return mulExp;
    }

    private AddExp parseAddExp() {
        AddExp addExp = new AddExp();
        addExp.addComp(parseMulExp());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.PLUS
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.MINU) {
            addExp.addComp(descriptions.get(index));
            index++;
            addExp.addComp(parseMulExp());
        }
        return addExp;
    }

    private RelExp parseRelExp() {
        RelExp relExp = new RelExp();
        relExp.addComp(parseAddExp());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.LSS
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.GRE
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.LEQ
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.GEQ) {
            relExp.addComp(descriptions.get(index));
            index++;
            relExp.addComp(parseAddExp());
        }
        return relExp;
    }

    private EqExp parseEqExp() {
        EqExp eqExp = new EqExp();
        eqExp.addComp(parseRelExp());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.EQL
                || descriptions.get(index).getWordTypeCode() == WordTypeCode.NEQ) {
            eqExp.addComp(descriptions.get(index));
            index++;
            eqExp.addComp(parseRelExp());
        }
        return eqExp;
    }

    private LAndExp parseLAndExp() {
        LAndExp lAndExp = new LAndExp();
        lAndExp.addComp(parseEqExp());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.AND) {
            lAndExp.addComp(descriptions.get(index));
            index++;
            lAndExp.addComp(parseEqExp());
        }
        return lAndExp;
    }

    private LOrExp parseLOrExp() {
        LOrExp lOrExp = new LOrExp();
        lOrExp.addComp(parseLAndExp());
        while (descriptions.get(index).getWordTypeCode() == WordTypeCode.OR) {
            lOrExp.addComp(descriptions.get(index));
            index++;
            lOrExp.addComp(parseLAndExp());
        }
        return lOrExp;
    }

    private ConstExp parseConstExp() {
        return new ConstExp(parseAddExp());
    }

    private Ident parseIdent() {
        Ident ident = null;
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.IDENFR) {
            ident = new Ident(descriptions.get(index));
            index++;
        }//错误处理预留
        return ident;
    }

    private IntConst parseIntConst() {
        IntConst intConst = null;
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.INTCON) {
            intConst = new IntConst(descriptions.get(index));
            index++;
        }   //错误处理预留
        return intConst;
    }

    private FormatString parseFormatString() {
        FormatString formatString = null;
        if (descriptions.get(index).getWordTypeCode() == WordTypeCode.STRCON) {
            formatString = new FormatString(descriptions.get(index), descriptions.get(index).getLine());
            index++;
        }
        return formatString;
    }
}
