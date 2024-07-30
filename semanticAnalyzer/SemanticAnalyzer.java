package semanticAnalyzer;

import errorHandler.ErrorDescription;
import errorHandler.ErrorType;
import errorHandler.ExpValueType;
import lexer.WordDescription;
import lexer.WordTypeCode;
import parser.component.AddExp;
import parser.component.Block;
import parser.component.BlockItem;
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
import parser.component.Ident;
import parser.component.InitVal;
import parser.component.IntConst;
import parser.component.LAndExp;
import parser.component.LOrExp;
import parser.component.Lval;
import parser.component.MainFuncDef;
import parser.component.MulExp;
import parser.component.NormalOrConstVarDecl;
import parser.component.Number;
import parser.component.PrimaryExp;
import parser.component.RelExp;
import parser.component.Stmt;
import parser.component.SyntaxComp;
import parser.component.UnaryExp;
import parser.component.UnaryOp;
import parser.component.VarDecl;
import parser.component.VarDef;
import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.alloc.AllocVar;
import semanticAnalyzer.LLVMIR.array.GetArrayElePtr;
import semanticAnalyzer.LLVMIR.bitExt.Zext;
import semanticAnalyzer.LLVMIR.calculate.CalcuType;
import semanticAnalyzer.LLVMIR.calculate.Calculate;
import semanticAnalyzer.LLVMIR.compare.CmpType;
import semanticAnalyzer.LLVMIR.compare.Compare;
import semanticAnalyzer.LLVMIR.func.funcCall.FuncCall;
import semanticAnalyzer.LLVMIR.func.funcDec.FuncDec;
import semanticAnalyzer.LLVMIR.func.funcDef.FuncDefBegin;
import semanticAnalyzer.LLVMIR.func.funcDef.FuncDefEnd;
import semanticAnalyzer.LLVMIR.func.funcRet.FuncRet;
import semanticAnalyzer.LLVMIR.input.GetInt;
import semanticAnalyzer.LLVMIR.jumpLabel.Br;
import semanticAnalyzer.LLVMIR.jumpLabel.Label;
import semanticAnalyzer.LLVMIR.loadStore.Load;
import semanticAnalyzer.LLVMIR.loadStore.Store;
import semanticAnalyzer.LLVMIR.output.PutCh;
import semanticAnalyzer.LLVMIR.output.PutInt;
import semanticAnalyzer.LLVMIR.varDef.GlobalVar;
import semanticAnalyzer.returnValue.ExpReturn;
import semanticAnalyzer.symbolTable.SymbolTable;
import semanticAnalyzer.symbolTable.symbolPara.FuncPara;
import semanticAnalyzer.symbolTable.symbolPara.ParamInfo;
import semanticAnalyzer.symbolTable.symbolPara.SymbolPara;
import semanticAnalyzer.symbolTable.symbolPara.VarPara;

import java.util.ArrayList;

public class SemanticAnalyzer {
    private final CompUnit compUnit;
    private final ArrayList<LLVMIRIns> instructions;
    private final ArrayList<ErrorDescription> errorDescriptions;
    private final SymbolTable symbolTable;
    private int ifNumber;
    private int forNumber;
    private int lOrExpNumber;
    private int lAndExpNumber;

    public SemanticAnalyzer(CompUnit compUnit, ArrayList<ErrorDescription> errorDescriptions) {
        this.compUnit = compUnit;
        this.errorDescriptions = errorDescriptions;
        this.instructions = new ArrayList<>();
        this.symbolTable = new SymbolTable(null);
        this.ifNumber = 0;
        this.forNumber = 0;
        this.lOrExpNumber = 0;
        this.lAndExpNumber = 0;
    }

    public ArrayList<LLVMIRIns> semanticAnalyze() {
        addDefaultInstructions();
        String belong = "CompUnit";
        for (Decl decl : compUnit.getDecls()) {
            semanticAnalyze(symbolTable, belong, decl, true);
        }
        for (FuncDef funcDef : compUnit.getFuncDefs()) {
            semanticAnalyze(symbolTable, belong, funcDef);
        }
        semanticAnalyze(symbolTable, compUnit.getMainFuncDef());
        return instructions;
    }

    private void addDefaultInstructions() {
        instructions.add(new FuncDec(WordTypeCode.INTTK, "getint", new ArrayList<>()));
        ArrayList<ParamInfo> paramInfoPutInt = new ArrayList<>();
        paramInfoPutInt.add(new ParamInfo("", 0, 0));
        instructions.add(new FuncDec(WordTypeCode.VOIDTK, "putint", paramInfoPutInt));
        ArrayList<ParamInfo> paramInfoPutCh = new ArrayList<>();
        paramInfoPutCh.add(new ParamInfo("", 0, 0));
        instructions.add(new FuncDec(WordTypeCode.VOIDTK, "putch", paramInfoPutCh));
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, Decl decl, boolean isGlobal) {
        NormalOrConstVarDecl varDecl = decl.getVarDecl();
        String platform = belong + "_Decl";
        if (varDecl instanceof VarDecl) {
            semanticAnalyze(symbolTable, platform, (VarDecl) varDecl, isGlobal);
        } else {
            semanticAnalyze(symbolTable, platform, (ConstDecl) varDecl, isGlobal);
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, VarDecl varDecl, boolean isGlobal) {
        ArrayList<SyntaxComp> comps = varDecl.getComps();
        String platform = belong + "_VarDecl";
        int len = comps.size();
        int index = 1;
        while (index < len) {
            semanticAnalyze(symbolTable, platform, (VarDef) comps.get(index), isGlobal);
            //platform未采用数字区分vardecl因为变量名唯一
            index += 2;
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, VarDef varDef, boolean isGlobal) {
        ArrayList<SyntaxComp> comps = varDef.getComps();
        String platform = belong + "_VarDef";
        int len = comps.size();
        Ident ident = (Ident) comps.get(0);
        String identName = ident.getSyntaxContent().getContent();
        boolean repeat = false;
        if (symbolTable.hasVar(identName)) {
            errorDescriptions.add(new ErrorDescription(ident.getLine(), ErrorType.b));
            repeat = true;
        }
        int index = 1;
        int dimen = 0;
        int[] dimens = new int[2];
        while (index < len && ((WordDescription) comps.get(index)).getWordTypeCode() == WordTypeCode.LBRACK) {
            dimens[dimen] = semanticAnalyze(symbolTable, (ConstExp) comps.get(index + 1));
            dimen++;
            index += 3;
        }
        if (index < len) {
            semanticAnalyze(symbolTable, platform, ((InitVal) comps.get(index + 1)), identName,
                    dimen, dimens, isGlobal, repeat);
        } else {
            semanticAnalyze(symbolTable, platform, (InitVal) null, identName, dimen, dimens,
                    isGlobal, repeat);
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, InitVal initVal, String identName,
                                 int dimen, int[] dimens, boolean isGlobal, boolean repeat) {
        VarPara varPara = new VarPara(dimen, dimens, isGlobal, false);
        if (isGlobal) {
            int[][] ret = null;
            switch (dimen) {
                case 0 -> {
                    ret = new int[1][1];
                    ret[0][0] = 0;
                }
                case 1 -> {
                    ret = new int[1][dimens[0]];
                    for (int i = 0; i < dimens[0]; i++) {
                        ret[0][i] = 0;
                    }
                }
                case 2 -> {
                    ret = new int[dimens[0]][dimens[1]];
                    for (int i = 0; i < dimens[0]; i++) {
                        for (int j = 0; j < dimens[1]; j++) {
                            ret[i][j] = 0;
                        }
                    }
                }
            }
            assert ret != null;
            if (initVal != null) {
                ArrayList<SyntaxComp> comps = initVal.getComps();
                int len = comps.size();
                switch (dimen) {
                    case 0 -> ret[0][0] = semanticAnalyze(symbolTable, "", (Exp) comps.get(0), true
                    ).getValue();
                    case 1 -> {
                        int index = 1;
                        int sub = 0;
                        while (index < len) {
                            ret[0][sub] = semanticAnalyze(symbolTable, "", (Exp) comps.get(index).getComps().get(0),
                                    true).getValue();
                            sub += 1;
                            index += 2;
                        }
                    }
                    case 2 -> {
                        int index = 1;
                        int line = 0;
                        while (index < len) {
                            for (int i = 0; i < dimens[1]; i++) {
                                ret[line][i] = semanticAnalyze(symbolTable, "",
                                        (Exp) comps.get(index).getComps().get(2 * i + 1).getComps().get(0), true
                                ).getValue();
                            }
                            line += 1;
                            index += 2;
                        }
                    }
                }
                if (!repeat) {
                    symbolTable.addSymbol(identName, varPara);
                    instructions.add(new GlobalVar(identName, false, dimen, dimens, ret));
                }
            } else {
                if (!repeat) {
                    symbolTable.addSymbol(identName, varPara);
                    instructions.add(new GlobalVar(identName, false, dimen, dimens, null));
                }
            }
        } else { //不是全局数组，值可能是需要用表达式表示的
            String varOutName = belong + "_" + identName;
            if (initVal == null) { //  未赋初值, 三种维数（012）都可以这么表示
                if (!repeat) {
                    instructions.add(new AllocVar(varOutName, dimen, dimens));
                    symbolTable.addSymbol(identName, varPara);
                    symbolTable.setOutName(identName, varOutName);
                }
            } else {
                ArrayList<SyntaxComp> comps = initVal.getComps();
                String valBelong = varOutName + "_initVal";
                int len = comps.size();
                instructions.add(new AllocVar(varOutName, dimen, dimens));
                switch (dimen) {
                    case 0 -> {
                        ExpReturn expReturn = semanticAnalyze(symbolTable, valBelong, (Exp) comps.get(0),
                                false);
                        int value = 0;
                        boolean isValue = expReturn.isValue();
                        String retLabel = "";
                        if (isValue) {
                            value = expReturn.getValue();
                        } else {
                            retLabel = expReturn.getContent();
                        }
                        if (!repeat) {
                            symbolTable.addSymbol(identName, varPara);
                            symbolTable.setOutName(identName, varOutName);
                            if (isValue) {
                                instructions.add(new Store(value, true, varOutName,
                                        false));
                            } else {
                                instructions.add(new Store(retLabel, false, varOutName,
                                        false));
                            }
                        }
                    }
                    case 1 -> {
                        int index = 1;
                        ArrayList<String> arrIndexIndex = new ArrayList<>();
                        ArrayList<Boolean> arrIndexIsValue = new ArrayList<>();
                        int sub = 0;
                        while (index < len) {
                            sub += 1;
                            ExpReturn expReturn = semanticAnalyze(symbolTable, valBelong + "__" + sub + "__",
                                    (Exp) comps.get(index).getComps().get(0), false);
                            arrIndexIndex.add(expReturn.getContent());
                            arrIndexIsValue.add(expReturn.isValue());
                            index += 2;
                        }
                        if (!repeat) {
                            symbolTable.addSymbol(identName, varPara);
                            symbolTable.setOutName(identName, varOutName);
                            assert arrIndexIndex.size() == dimens[0];
                            for (int i = 0; i < dimens[0]; i++) {
                                int[] indexArr = {i};
                                String eleName = varOutName + "_" + i + "_";
                                instructions.add(new GetArrayElePtr(dimen, dimens, indexArr,
                                        varOutName, false, eleName));
                                instructions.add(new Store(arrIndexIndex.get(i),
                                        arrIndexIsValue.get(i), eleName, false));
                            }
                        }
                    }
                    //  %6 = getelementptr inbounds [3 x i32], [3 x i32]* %3, i64 0, i64 0
                    //  store i32 0, i32* %6, align 4
                    case 2 -> {
                        ArrayList<ArrayList<String>> arrIndexIndex = new ArrayList<>();
                        ArrayList<ArrayList<Boolean>> arrIndexIsValue = new ArrayList<>();
                        int index = 1;
                        int sub = 0;
                        while (index < len) {
                            sub++;
                            ArrayList<String> arrIndexIndex_tmp = new ArrayList<>();
                            ArrayList<Boolean> arrIndexIsValue_tmp = new ArrayList<>();
                            for (int i = 0; i < dimens[1]; i++) {
                                ExpReturn expReturn = semanticAnalyze(symbolTable,
                                        valBelong + "__" + sub + "____" + i + "__",
                                        (Exp) comps.get(index).getComps().get(2 * i + 1).getComps().get(0), false
                                );
                                arrIndexIndex_tmp.add(expReturn.getContent());
                                arrIndexIsValue_tmp.add(expReturn.isValue());
                            }
                            index += 2;
                            arrIndexIndex.add(arrIndexIndex_tmp);
                            arrIndexIsValue.add(arrIndexIsValue_tmp);
                        }
                        if (!repeat) {
                            symbolTable.addSymbol(identName, varPara);
                            symbolTable.setOutName(identName, varOutName);
                            assert arrIndexIndex.size() == dimens[0];
                            for (int i = 0; i < dimens[0]; i++) {
                                assert arrIndexIndex.get(i).size() == dimens[1];
                                for (int j = 0; j < dimens[1]; j++) {
                                    int[] indexArr = {i, j};
                                    String eleName = varOutName + "__" + i + "____" + j + "__";
                                    instructions.add(new GetArrayElePtr(dimen, dimens, indexArr,
                                            varOutName, false, eleName));
                                    instructions.add(new Store(arrIndexIndex.get(i).get(j),
                                            arrIndexIsValue.get(i).get(j), eleName, false));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, ConstDecl constDecl, boolean isGlobal) {
        ArrayList<SyntaxComp> comps = constDecl.getComps();
        String platform = belong + "_ConstDecl";
        int len = comps.size();
        int index = 2;
        while (index < len) {
            semanticAnalyze(symbolTable, platform, (ConstDef) comps.get(index), isGlobal);
            index += 2;
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, ConstDef constDef, boolean isGlobal) {
        ArrayList<SyntaxComp> comps = constDef.getComps();
        String platform = belong + "_ConstDef";
        int len = comps.size();
        Ident ident = (Ident) comps.get(0);
        String identName = ident.getSyntaxContent().getContent();
        boolean repeat = false;
        if (symbolTable.hasVar(identName)) {
            errorDescriptions.add(new ErrorDescription(ident.getLine(), ErrorType.b));
            repeat = true;
        }
        int index = 1;
        int dimen = 0;
        int[] dimens = new int[2];
        while (index < len && ((WordDescription) comps.get(index)).getWordTypeCode() == WordTypeCode.LBRACK) {
            dimens[dimen] = semanticAnalyze(symbolTable, (ConstExp) comps.get(index + 1));
            dimen++;
            index += 3;
        }
        semanticAnalyze(symbolTable, platform, ((ConstInitVal) comps.get(index + 1)), identName,
                dimen, dimens, isGlobal, repeat);
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, ConstInitVal constInitVal,
                                 String identName, int dimen, int[] dimens, boolean isGlobal, boolean repeat) {
        VarPara varPara = new VarPara(dimen, dimens, isGlobal, true);
        int[][] ret = null;
        switch (dimen) {
            case 0 -> {
                ret = new int[1][1];
                ret[0][0] = 0;
            }
            case 1 -> {
                ret = new int[1][dimens[0]];
                for (int i = 0; i < dimens[0]; i++) {
                    ret[0][i] = 0;
                }
            }
            case 2 -> {
                ret = new int[dimens[0]][dimens[1]];
                for (int i = 0; i < dimens[0]; i++) {
                    for (int j = 0; j < dimens[1]; j++) {
                        ret[i][j] = 0;
                    }
                }
            }
        }
        assert ret != null;
        ArrayList<SyntaxComp> comps = constInitVal.getComps();
        int len = comps.size();
        switch (dimen) {
            case 0 -> ret[0][0] = semanticAnalyze(symbolTable, (ConstExp) comps.get(0));
            case 1 -> {
                int index = 1;
                int sub = 0;
                while (index < len) {
                    ret[0][sub] = semanticAnalyze(symbolTable,
                            (ConstExp) comps.get(index).getComps().get(0));
                    sub += 1;
                    index += 2;
                }
            }
            case 2 -> {
                int index = 1;
                int line = 0;
                while (index < len) {
                    for (int i = 0; i < dimens[1]; i++) {
                        ret[line][i] = semanticAnalyze(symbolTable,
                                (ConstExp) comps.get(index).getComps().get(2 * i + 1).getComps().get(0));
                    }
                    line++;
                    index += 2;
                }
            }
        }
        if (!repeat) {
            symbolTable.addSymbol(identName, varPara);
            symbolTable.addVarValue(identName, dimen, dimens, ret);
            if (isGlobal) {
                instructions.add(new GlobalVar(identName, true, dimen, dimens, ret));
            } else {
                String varOutName = belong + "_" + identName;
                instructions.add(new AllocVar(varOutName, dimen, dimens));
                symbolTable.setOutName(identName, varOutName);
                switch (dimen) {
                    case 0 -> instructions.add(new Store(ret[0][0], true,
                            varOutName, false));
                    case 1 -> {
                        for (int i = 0; i < dimens[0]; i++) {
                            int[] indexArr = {i};
                            String eleName = varOutName + "__" + i + "__";
                            instructions.add(new GetArrayElePtr(dimen, dimens, indexArr,
                                    varOutName, false, eleName));
                            instructions.add(new Store(ret[0][i], true,
                                    eleName, false));
                        }
                    }
                    case 2 -> {
                        for (int i = 0; i < dimens[0]; i++) {
                            for (int j = 0; j < dimens[1]; j++) {
                                int[] indexArr = {i, j};
                                String eleName = varOutName + "__" + i + "____" + j + "__";
                                instructions.add(new GetArrayElePtr(dimen, dimens, indexArr,
                                        varOutName, false, eleName));
                                instructions.add(new Store(ret[i][j], true, eleName, false));
                            }
                        }
                    }
                }
            }
        }
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, Exp exp, boolean forInstantValue) {
        ArrayList<SyntaxComp> comps = exp.getComps();
        return semanticAnalyze(symbolTable, belong, (AddExp) comps.get(0), forInstantValue);
    }

    private int semanticAnalyze(SymbolTable symbolTable, ConstExp constExp) {
        ArrayList<SyntaxComp> comps = constExp.getComps();
        return semanticAnalyze(symbolTable, "", (AddExp) comps.get(0), true).getValue();
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, AddExp addExp, boolean forInstantValue) {
        ArrayList<SyntaxComp> comps = addExp.getComps();
        int len = comps.size();
        if (forInstantValue) {
            int ret = semanticAnalyze(symbolTable, "", (MulExp) comps.get(0), true).getValue();
            int index = 1;
            while (index + 1 < len) {
                int retTmp = semanticAnalyze(symbolTable, "", (MulExp) comps.get(index + 1), true).getValue();
                WordTypeCode wordTypeCode = ((WordDescription) comps.get(index)).getWordTypeCode();
                assert wordTypeCode == WordTypeCode.PLUS || wordTypeCode == WordTypeCode.MINU;
                if (wordTypeCode == WordTypeCode.PLUS) {
                    ret += retTmp;
                } else {
                    ret -= retTmp;
                }
                index += 2;
            }
            return new ExpReturn(ret);
        } else {
            int countMulExp = 0;
            boolean allValue;
            countMulExp += 1;
            String mulExpName = belong + "_MulExp_" + countMulExp;
            int addSubNumber = 0;
            ExpReturn expReturn = semanticAnalyze(symbolTable, mulExpName, (MulExp) comps.get(0),
                    false);
            ExpValueType expValueType = expReturn.getExpValueType();
            int value = 0;
            boolean isValue = expReturn.isValue();
            String retLabel = "";
            allValue = isValue;
            if (isValue) {
                value = expReturn.getValue();
            } else {
                retLabel = expReturn.getContent();
            }
            int index = 1;
            int lastValue;
            String lastLabel;
            boolean lastIsValue;
            while (index + 1 < len) {
                countMulExp += 1;
                lastValue = value;
                lastLabel = retLabel;
                lastIsValue = allValue;
                mulExpName = belong + "_MulExp_" + countMulExp;
                ExpReturn expReturn_tmp = semanticAnalyze(symbolTable, mulExpName,
                        (MulExp) comps.get(index + 1), false);
                expValueType = expValueType.calculate(expReturn_tmp.getExpValueType());
                isValue = expReturn_tmp.isValue();
                allValue &= isValue;
                if (isValue) {
                    value = expReturn_tmp.getValue();
                } else {
                    retLabel = expReturn_tmp.getContent();
                }
                WordTypeCode wordTypeCode = ((WordDescription) comps.get(index)).getWordTypeCode();
                assert wordTypeCode == WordTypeCode.PLUS || wordTypeCode == WordTypeCode.MINU;
                if (allValue) {
                    if (wordTypeCode == WordTypeCode.PLUS) {
                        value = lastValue + value;
                    } else {
                        value = lastValue - value;
                    }
                } else {
                    String insOp1 = (lastIsValue) ? String.valueOf(lastValue) : lastLabel;
                    String insOp2 = (isValue) ? String.valueOf(value) : retLabel;
                    CalcuType calcuType;
                    if (wordTypeCode == WordTypeCode.PLUS) {
                        calcuType = CalcuType.add;
                    } else {
                        calcuType = CalcuType.sub;
                    }
                    addSubNumber += 1;
                    retLabel = belong + "_addSub_" + addSubNumber;
                    instructions.add(new Calculate(calcuType, retLabel, insOp1,
                            lastIsValue, insOp2, isValue));
                }
                index += 2;
            }
            if (!allValue) {
                return new ExpReturn(false, retLabel, expValueType);
            } else {
                return new ExpReturn(value);
            }
        }
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, MulExp mulExp,
                                      boolean forInstantValue) {
        ArrayList<SyntaxComp> comps = mulExp.getComps();
        int len = comps.size();
        if (forInstantValue) {
            int ret = semanticAnalyze(symbolTable, "", (UnaryExp) comps.get(0), true).getValue();
            int index = 1;
            while (index + 1 < len) {
                int retTmp = semanticAnalyze(symbolTable, "", (UnaryExp) comps.get(index + 1), true).getValue();
                WordTypeCode wordTypeCode = ((WordDescription) comps.get(index)).getWordTypeCode();
                assert wordTypeCode == WordTypeCode.MULT || wordTypeCode == WordTypeCode.DIV || wordTypeCode == WordTypeCode.MOD;
                switch (wordTypeCode) {
                    case MULT -> ret *= retTmp;
                    case DIV -> ret /= retTmp;
                    default -> ret %= retTmp;
                }
                index += 2;
            }
            return new ExpReturn(ret);
        } else {
            int countUnaryExp = 0;
            boolean allValue;
            countUnaryExp += 1;
            int mulDivNumber = 0;
            String unaryExpName = belong + "_UnaryExp_" + countUnaryExp;
            ExpReturn expReturn = semanticAnalyze(symbolTable, unaryExpName, (UnaryExp) comps.get(0), false);
            ExpValueType expValueType = expReturn.getExpValueType();
            boolean isValue = expReturn.isValue();
            allValue = isValue;
            int value = 0;
            String retLabel = "";
            if (isValue) {
                value = expReturn.getValue();
            } else {
                retLabel = expReturn.getContent();
            }
            int index = 1;
            int lastValue;
            String lastLabel;
            boolean lastIsValue;
            while (index + 1 < len) {
                countUnaryExp += 1;
                lastValue = value;
                lastLabel = retLabel;
                lastIsValue = allValue;
                unaryExpName = belong + "_UnaryExp_" + countUnaryExp;
                ExpReturn expReturn_tmp = semanticAnalyze(symbolTable, unaryExpName, (UnaryExp) comps.get(index + 1),
                        false);
                expValueType = expValueType.calculate(expReturn_tmp.getExpValueType());
                isValue = expReturn_tmp.isValue();
                allValue &= isValue;
                if (isValue) {
                    value = expReturn_tmp.getValue();
                } else {
                    retLabel = expReturn_tmp.getContent();
                }
                WordTypeCode wordTypeCode = ((WordDescription) comps.get(index)).getWordTypeCode();
                assert wordTypeCode == WordTypeCode.MULT || wordTypeCode == WordTypeCode.DIV || wordTypeCode == WordTypeCode.MOD;
                if (allValue) {
                    switch (wordTypeCode) {
                        case MULT -> value = lastValue * value;
                        case DIV -> value = lastValue / value;
                        default -> value = lastValue % value;
                    }
                } else {
                    String insOp1 = (lastIsValue) ? String.valueOf(lastValue) : lastLabel;
                    String insOp2 = (isValue) ? String.valueOf(value) : retLabel;
                    CalcuType calcuType;
                    switch (wordTypeCode) {
                        case MULT -> calcuType = CalcuType.mul;
                        case DIV -> calcuType = CalcuType.sdiv;
                        default -> calcuType = CalcuType.srem;
                    }
                    mulDivNumber += 1;
                    retLabel = belong + "_mulDiv_" + mulDivNumber;
                    instructions.add(new Calculate(calcuType, retLabel, insOp1,
                            lastIsValue, insOp2, isValue));
                }
                index += 2;
            }
            if (!allValue) {
                return new ExpReturn(false, retLabel, expValueType);
            } else {
                return new ExpReturn(value);
            }
        }
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, UnaryExp unaryExp,
                                      boolean forInstantValue) {
        ArrayList<SyntaxComp> comps = unaryExp.getComps();
        String platform = belong + "_UnaryExp";
        if (forInstantValue) {
            int ret = 0;
            assert !(comps.get(0) instanceof Ident);    // 全局变量赋初值不可能调用函数(?
            if (comps.get(0) instanceof PrimaryExp) {
                ret = semanticAnalyze(symbolTable, "", (PrimaryExp) comps.get(0), true).getValue();
            } else if (comps.get(0) instanceof UnaryOp) {
                ret = semanticAnalyze(symbolTable, "", (UnaryExp) comps.get(1), true).getValue();
                WordTypeCode wordTypeCode = semanticAnalyze((UnaryOp) comps.get(0));
                assert wordTypeCode != null;
                switch (wordTypeCode) {
                    case MINU -> ret = -ret;
                    case NOT -> ret = ((ret != 0) ? 0 : 1);
                }
            }
            return new ExpReturn(ret);
        } else {
            if (comps.get(0) instanceof PrimaryExp) {
                return semanticAnalyze(symbolTable, platform, (PrimaryExp) comps.get(0), false);
            } else if (comps.get(0) instanceof Ident ident) {
                String identName = ident.getSyntaxContent().getContent();
                if (symbolTable.notDefineVar(identName)) {
                    errorDescriptions.add(new ErrorDescription(ident.getLine(), ErrorType.c));
                    return new ExpReturn(false, "-2");//错误处理
                }
                int funcParamNumbers = symbolTable.findFuncParamInfos(identName).size();
                boolean lackRParent = symbolTable.findFuncPara(identName).isLackRParent();
                if (comps.get(2) instanceof FuncRParams funcRParams) {
                    if (!lackRParent) {
                        int givenNumber = funcRParams.getParamNumbers();
                        if (funcParamNumbers != givenNumber) {
                            errorDescriptions.add(new ErrorDescription(ident.getLine(), ErrorType.d));
                            return new ExpReturn(false, "-2");//错误处理
                        }
                    }
                    ArrayList<ExpValueType> funcExpValueTypes = new ArrayList<>();
                    for (ParamInfo funcParamInfo : symbolTable.findFuncParamInfos(identName)) {
                        funcExpValueTypes.add(funcParamInfo.getExpValueType());
                    }
                    return doIdent(symbolTable, platform, identName, funcRParams, lackRParent, funcExpValueTypes,
                            null, null, true, false, ident.getLine());
                } else {
                    if (!lackRParent) {
                        if (funcParamNumbers != 0) {
                            errorDescriptions.add(new ErrorDescription(ident.getLine(), ErrorType.d));
                            return new ExpReturn(false, "-2");//错误处理
                        }
                    }
                    return doIdent(symbolTable, platform, identName, null,
                            false, null, null, null, true, false,
                            -1);
                }
            } else {//UnaryOp
                ExpReturn expReturn = semanticAnalyze(symbolTable, platform, (UnaryExp) comps.get(1), false);
                ExpValueType expValueType = expReturn.getExpValueType();
                boolean isValue = expReturn.isValue();
                int value = 0;
                String label = "";
                if (isValue) {
                    value = expReturn.getValue();
                } else {
                    label = expReturn.getContent();
                }
                WordTypeCode wordTypeCode = semanticAnalyze((UnaryOp) comps.get(0));
                assert wordTypeCode != null;
                switch (wordTypeCode) {
                    case MINU -> {
                        if (isValue) {
                            value *= -1;
                            return new ExpReturn(value);
                        } else {
                            String resLabel = platform + "_Minu";
                            instructions.add(new Calculate(CalcuType.sub, resLabel, String.valueOf(0),
                                    true, label, false));
                            return new ExpReturn(false, resLabel, (expValueType == ExpValueType.NULL) ?
                                    ExpValueType.NULL : ExpValueType.VAR_INT);
                        }
                    }
                    case NOT -> {
                        if (isValue) {
                            return new ExpReturn((value != 0) ? 0 : 1);
                        } else {
                            String resLabel = platform + "_Not";
                            instructions.add(new Compare(CmpType.eq, resLabel, label, false, String.valueOf(0), true));
                            String cmpLabel = platform + "_Not_Ext32";
                            instructions.add(new Zext(cmpLabel, resLabel));
                            return new ExpReturn(false, cmpLabel, (expValueType == ExpValueType.NULL) ?
                                    ExpValueType.NULL : ExpValueType.VAR_INT);
                        }
                    }
                    default -> {
                        if (isValue) {
                            return new ExpReturn(value);
                        } else {
                            return new ExpReturn(false, label, (expValueType == ExpValueType.NULL) ?
                                    ExpValueType.NULL : ExpValueType.VAR_INT);
                        }
                    }
                }
            }
        }
    }

    private WordTypeCode semanticAnalyze(UnaryOp unaryOp) {
        ArrayList<SyntaxComp> comps = unaryOp.getComps();
        return ((WordDescription) comps.get(0)).getWordTypeCode();
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, PrimaryExp primaryExp,
                                      boolean forInstantValue) {
        String platform = belong + "_PrimaryExp";
        ArrayList<SyntaxComp> comps = primaryExp.getComps();
        if (forInstantValue) {
            int ret;
            if (comps.get(0) instanceof Lval) {
                ret = semanticAnalyze(symbolTable, platform, (Lval) comps.get(0), true, true).getValue();
            } else if (comps.get(0) instanceof Number) {
                ret = semanticAnalyze((Number) comps.get(0));
            } else {
                ret = semanticAnalyze(symbolTable, platform, (Exp) comps.get(1), true).getValue();
            }
            return new ExpReturn(ret);
        } else {
            if (comps.get(0) instanceof Lval) {
                return semanticAnalyze(symbolTable, platform, (Lval) comps.get(0), true, false);
            } else if (comps.get(0) instanceof Number) {
                return new ExpReturn(semanticAnalyze((Number) comps.get(0)));
            } else {
                return semanticAnalyze(symbolTable, platform, (Exp) comps.get(1), false);
            }
        }
    }

    private int semanticAnalyze(Number number) {
        ArrayList<SyntaxComp> comps = number.getComps();
        WordDescription wordDescription = ((IntConst) comps.get(0)).getSyntaxContent();
        return Integer.parseInt(wordDescription.getContent());
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, Lval lval, boolean rightValue,
                                      boolean forInstantValue) {
        String platform = belong + "_Lval";
        ArrayList<SyntaxComp> comps = lval.getComps();
        int len = comps.size();
        String ident = ((Ident) comps.get(0)).getSyntaxContent().getContent();
        if (symbolTable.notDefineVar(ident)) {
            errorDescriptions.add(new ErrorDescription(((Ident) comps.get(0)).getLine(), ErrorType.c));
            return new ExpReturn(false, "-2");//错误处理
        }
        int index = 2;
        if (forInstantValue) {
            ArrayList<String> indexArr = new ArrayList<>();
            ArrayList<Boolean> indexIsValue = new ArrayList<>();
            while (index < len) {
                indexArr.add(String.valueOf(semanticAnalyze(symbolTable, platform, (Exp) comps.get(index), true
                ).getValue()));
                indexIsValue.add(true);
                index += 3;
            }
            return doIdent(symbolTable, platform, ident, null, false, null, indexArr, indexIsValue, true, true, -1);
        } else {
            ArrayList<String> arrIndexIndex = new ArrayList<>();
            ArrayList<Boolean> arrIndexIsValue = new ArrayList<>();
            int sub = 0;
            while (index < len) {
                sub += 1;
                ExpReturn expReturn = semanticAnalyze(symbolTable, platform + "_sub" + sub, (Exp) comps.get(index),
                        false);
                arrIndexIndex.add(expReturn.getContent());
                arrIndexIsValue.add(expReturn.isValue());
                index += 3;
            }
            return doIdent(symbolTable, platform, ident, null, false, null, arrIndexIndex,
                    arrIndexIsValue, rightValue, false, -1);
        }
    }

    private ExpReturn doIdent(SymbolTable symbolTable, String belong, String ident, FuncRParams funcRParams,
                              boolean lackRParent, ArrayList<ExpValueType> expValueTypes,
                              ArrayList<String> index, ArrayList<Boolean> isValueArr,
                              boolean rightValue, boolean forInstantValue, int identLine) {
        String platform = belong + "_DoIdent";
        if (forInstantValue) {
            assert funcRParams == null;
            ArrayList<Integer> intIndex = new ArrayList<>();
            for (String val : index) {
                intIndex.add(Integer.parseInt(val));
            }
            return new ExpReturn(symbolTable.findVarValue(ident, intIndex));
        } else {
            if (index == null) {//函数调用
                ArrayList<String> expIndex = new ArrayList<>();
                ArrayList<Boolean> expIsValue = new ArrayList<>();
                if (funcRParams != null) {
                    ArrayList<SyntaxComp> comps = funcRParams.getComps();
                    int len = comps.size();
                    int compIndex = 0;
                    int paramNumber = 0;
                    ArrayList<ExpValueType> valueTypes = new ArrayList<>();
                    while (compIndex < len) {
                        paramNumber += 1;
                        ExpReturn expReturn = semanticAnalyze(symbolTable, platform + "_FuncRParam" + paramNumber,
                                (Exp) comps.get(compIndex),
                                false);
                        valueTypes.add(expReturn.getExpValueType());
                        expIndex.add(expReturn.getContent());
                        expIsValue.add(expReturn.isValue());
                        compIndex += 2;
                    }
                    if (!lackRParent) {
                        int typeNumber = valueTypes.size();
                        for (int i = 0; i < typeNumber; i++) {
                            if (valueTypes.get(i) != ExpValueType.NULL
                                    && valueTypes.get(i) != expValueTypes.get(i)) {
                                errorDescriptions.add(new ErrorDescription(identLine, ErrorType.e));
                                return new ExpReturn(false, "-2");//错误处理
                            }
                        }
                    }
                }
                ArrayList<ParamInfo> paramInfos = symbolTable.findFuncParamInfos(ident);
                assert paramInfos != null;
                WordTypeCode funcType = symbolTable.findFuncType(ident);
                assert funcType != null;
                String retLabel = platform + "_FuncRet";
                if (funcType == WordTypeCode.INTTK) {
                    instructions.add(new FuncCall(ident, paramInfos, expIndex, expIsValue, retLabel, false));
                } else {
                    instructions.add(new FuncCall(ident, paramInfos, expIndex, expIsValue, "", true));
                }
                return new ExpReturn(false, retLabel,
                        (funcType == WordTypeCode.INTTK) ? ExpValueType.VAR_INT : ExpValueType.VOID);
            } else {
                assert funcRParams == null;
                SymbolPara symbolPara = symbolTable.findPara(ident);
                if (symbolPara instanceof FuncPara) {
                    errorDescriptions.add(new ErrorDescription(identLine, ErrorType.c));
                    return new ExpReturn(false, "-2");
                }
                VarPara varPara = (VarPara) symbolPara;
                if (varPara == null) {
                    return new ExpReturn(false, "-2");//错误处理
                }
                int dimen = varPara.getDimen();
                int[] dimens = varPara.getDimens();
                boolean isGlobal = varPara.getIsGlobal();
                boolean isConst = varPara.getIsConst();
                String varOutName = varPara.getOutName();
                if (rightValue) {
                    switch (dimen) {
                        case 0 -> {
                            if (isConst) {//求值
                                return new ExpReturn(symbolTable.findVarValue(ident, new ArrayList<>()));
                            } else {
                                // %2 = load i32, i32* @a, align 4
                                String retLabel = platform + "_Load";
                                if (isGlobal) {
                                    instructions.add(new Load(ident, true, retLabel));
                                } else {
                                    instructions.add(new Load(varOutName, false, retLabel));
                                }
                                return new ExpReturn(false, retLabel, ExpValueType.VAR_INT);
                            }
                        }
                        case 1 -> {
                            int len = index.size();
                            String[] indexArr = {(len == 1) ? index.get(0) : "0"};
                            boolean[] indexIsValue = (len == 1) ? new boolean[]{isValueArr.get(0)} : null;
                            String ansLabel = varOutName;
                            if (isGlobal) { // len == 1,get+load(0,.0); len==0,get(0,0)
                                ansLabel = platform + "_Get__Glo_" + ident + "__i__";
                                instructions.add(new GetArrayElePtr(dimen, dimens, indexArr, indexIsValue, false,
                                        ident, true, ansLabel));
                            } else {
                                boolean isPtr = varPara.getIsArrPtr();
                                if (isPtr) {
                                    String resLabel = platform + "_Load_" + varOutName;
                                    instructions.add(new Load(varOutName, 1, -1, false, resLabel));
                                    ansLabel = resLabel;
                                }
                                if (len != 0 || !isPtr) {
                                    String resLabel = platform + "_Get_" + varOutName + "__i__";
                                    instructions.add(new GetArrayElePtr(dimen, dimens, indexArr, indexIsValue, isPtr,
                                            ansLabel, false, resLabel));
                                    ansLabel = resLabel;
                                }
                            }
                            if (len == 1) {
                                String resLabel = platform + "_Load_Final";
                                instructions.add(new Load(ansLabel, false, resLabel));
                                ansLabel = resLabel;
                            }
                            return new ExpReturn(false, ansLabel,
                                    (len == 1) ? ExpValueType.VAR_INT : ExpValueType.VAR_INT_ARR_1);
                        }
                        default -> {//case 2
                            int len = index.size();
                            String[] indexArr = (len == 2) ? new String[]{index.get(0), index.get(1)} :
                                    (len == 1) ? new String[]{index.get(0), "0"} :
                                            new String[]{"0"};
                            boolean[] indexIsValue = (len == 2) ? new boolean[]{isValueArr.get(0), isValueArr.get(1)} :
                                    (len == 1) ? new boolean[]{isValueArr.get(0), true} :
                                            null;
                            String ansLabel = varOutName;
                            if (isGlobal) { // len==2 ,get(0,.0,.1)+load; len==1,get(0,.0,0);len==0,get(0,0)
                                ansLabel = platform + "_Get__Glo_" + ident + "__i__j__";
                                instructions.add(new GetArrayElePtr(dimen, dimens, indexArr, indexIsValue, false,
                                        ident, true, ansLabel));
                            } else {
                                boolean isPtr = varPara.getIsArrPtr();
                                if (isPtr) {
                                    String resLabel = platform + "_Load_" + varOutName;
                                    int dimen2 = varPara.getDimens()[1];
                                    instructions.add(new Load(varOutName, 2, dimen2, false,
                                            resLabel));
                                    ansLabel = resLabel;
                                }
                                if (len != 0 || !isPtr) {
                                    String resLabel = platform + "_Get_" + varOutName + "__i__j__";
                                    instructions.add(new GetArrayElePtr(dimen, dimens, indexArr, indexIsValue, isPtr,
                                            ansLabel, false, resLabel));
                                    ansLabel = resLabel;
                                }
                            }
                            if (len == 2) {
                                String resLabel = platform + "_Load_Final";
                                instructions.add(new Load(ansLabel, false, resLabel));
                                ansLabel = resLabel;
                            }
                            return new ExpReturn(false, ansLabel,
                                    (len == 2) ? ExpValueType.VAR_INT :
                                            (len == 1) ? ExpValueType.VAR_INT_ARR_1 :
                                                    ExpValueType.VAR_INT_ARR_2);
                        }
                    }
                } else {//左值，只需要返回index
                    String retIndex;
                    switch (dimen) {
                        case 0 -> {
                            if (isGlobal) {
                                retIndex = "__GlobalLeft";
                            } else {
                                retIndex = varPara.getOutName();
                            }
                            return new ExpReturn(false, retIndex);
                        }
                        case 1 -> {
                            String ansLabel = varOutName;
                            String[] indexArr = {index.get(0)};
                            boolean[] indexIsValue = {isValueArr.get(0)};
                            if (isGlobal) {
                                ansLabel = platform + "_GetLeft__Glo_" + ident + "__i__";
                                instructions.add(new GetArrayElePtr(dimen, dimens, indexArr, indexIsValue, false,
                                        ident, true, ansLabel));
                            } else {
                                boolean isPtr = varPara.getIsArrPtr();
                                if (isPtr) {
                                    int ptrDepth = varPara.getDimen();
                                    String resLabel = platform + "_LoadLeft_" + varOutName;
                                    instructions.add(new Load(varOutName, ptrDepth, -1, false,
                                            resLabel));
                                    ansLabel = resLabel;
                                }
                                String resLabel = platform + "_GetLeft_" + varOutName + "__i__";
                                instructions.add(new GetArrayElePtr(dimen, dimens, indexArr, indexIsValue, isPtr,
                                        ansLabel, false, resLabel));
                                ansLabel = resLabel;
                            }
                            return new ExpReturn(false, ansLabel);
                        }
                        default -> { // case 2
                            String[] indexArr = {index.get(0), index.get(1)};
                            boolean[] indexIsValue = {isValueArr.get(0), isValueArr.get(1)};
                            String ansLabel = varOutName;
                            if (isGlobal) {
                                ansLabel = platform + "_GetLeft__Glo_" + ident + "__i__j__";
                                instructions.add(new GetArrayElePtr(dimen, dimens, indexArr, indexIsValue, false,
                                        ident, true, ansLabel));
                            } else {
                                boolean isPtr = varPara.getIsArrPtr();
                                if (isPtr) {
                                    int ptrDepth = varPara.getDimen();
                                    int dimen2 = varPara.getDimens()[1];
                                    String resLabel = platform + "_LoadLeft_" + varOutName;
                                    instructions.add(new Load(varOutName, ptrDepth, dimen2, false,
                                            resLabel));
                                    ansLabel = resLabel;
                                }
                                String resLabel = platform + "_GetLeft_" + varOutName + "__i__j__";
                                instructions.add(new GetArrayElePtr(dimen, dimens, indexArr, indexIsValue, isPtr,
                                        ansLabel, false, resLabel));
                                ansLabel = resLabel;
                            }
                            return new ExpReturn(false, ansLabel);
                        }
                    }
                }
            }
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, FuncDef funcDef) {
        ArrayList<SyntaxComp> comps = funcDef.getComps();
        WordDescription funcDesc = (WordDescription) comps.get(0).getComps().get(0);
        WordTypeCode funcType = funcDesc.getWordTypeCode();
        Ident ident = (Ident) (comps.get(1));
        String identName = ident.getSyntaxContent().getContent();
        if (symbolTable.hasVar(identName)) {
            errorDescriptions.add(new ErrorDescription(ident.getLine(), ErrorType.b));
            return;
//            identName = identName + "_Repeat_" + symbolTable.repeatTimes(identName);//错误处理
        }
        String platform = belong + "_FuncDef_" + identName;
        SymbolTable symbolTableNew = new SymbolTable(symbolTable);
        int index = 3;
        ArrayList<ParamInfo> paramInfos = new ArrayList<>();
        if (comps.get(index) instanceof FuncFParams) {
            paramInfos = semanticAnalyze(symbolTableNew, platform + "_FuncFParams", (FuncFParams) comps.get(index));
            index += 2;
        } else {
            index += 1;
        }
        symbolTable.addSymbol(identName, new FuncPara(funcType, paramInfos, funcDef.isLackRParent()));
        instructions.add(new FuncDefBegin(funcType, identName, paramInfos));
        for (ParamInfo paramInfo : paramInfos) {
            if (symbolTableNew.hasVar(paramInfo.getName())) {
                errorDescriptions.add(new ErrorDescription(paramInfo.getLine(), ErrorType.b));
                continue;
            }
            String paramOutName = paramInfo.getOutName();
            String paramCopyOutName = paramOutName + "_Copy";
            instructions.add(new AllocVar(paramCopyOutName, paramInfo.getDimen(),
                    paramInfo.getDimens(), true));
            instructions.add(new Store(paramOutName, paramInfo.getDimen(), paramInfo.getDimen_if2(), false,
                    paramCopyOutName, false));
            symbolTableNew.addSymbol(paramInfo.getName(), new VarPara(paramInfo.getDimen(),
                    paramInfo.getDimens(), false, false, paramInfo.getDimen() > 0,
                    paramCopyOutName));
        }
        semanticAnalyze(symbolTableNew, platform, (Block) comps.get(index),
                false, funcType == WordTypeCode.VOIDTK, funcType == WordTypeCode.INTTK, null, null);//todo
        if (funcType == WordTypeCode.VOIDTK && !(instructions.get(instructions.size() - 1) instanceof FuncRet)) {
            instructions.add(new FuncRet(WordTypeCode.VOIDTK, -1, false));
        }
        instructions.add(new FuncDefEnd());
    }

    private ArrayList<ParamInfo> semanticAnalyze(SymbolTable symbolTable, String belong, FuncFParams funcFParams) {
        ArrayList<SyntaxComp> comps = funcFParams.getComps();
        ArrayList<ParamInfo> ret = new ArrayList<>();
        int len = comps.size();
        int index = 0;
        while (index < len) {
            ParamInfo tmp = semanticAnalyze(symbolTable, belong + "_FuncFParam", (FuncFParam) comps.get(index));
            ret.add(tmp);
            index += 2;
        }
        return ret;
    }

    private ParamInfo semanticAnalyze(SymbolTable symbolTable, String belong, FuncFParam funcFParam) {
        ArrayList<SyntaxComp> comps = funcFParam.getComps();
        int len = comps.size();
        Ident ident = (Ident) comps.get(1);
        String identName = ident.getSyntaxContent().getContent();
        if (symbolTable.hasVar(identName)) {
            errorDescriptions.add(new ErrorDescription(ident.getLine(), ErrorType.b));
            identName = identName + "_Repeat_" + symbolTable.repeatTimes(identName);
        }
        String varOutName = belong + "_" + identName;
        int dimen = 0;
        int dimenIf2 = 0;
        int index = 2;
        if (index < len) {
            dimen += 1;
            index += 2;
            if (index < len) {
                dimen += 1;
                dimenIf2 = semanticAnalyze(symbolTable, (ConstExp) comps.get(index + 1));
            }
        }
        return new ParamInfo(identName, dimen, dimenIf2, varOutName, ident.getLine());
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, Block block,
                                 boolean isInFor, boolean isInVoid, boolean isIntFunc,
                                 String changeLabel, String endForLabel) {
        ArrayList<SyntaxComp> comps = block.getComps();
        int len = comps.size();
        for (int index = 1; index < len - 1; index++) {
            boolean isLast = index == len - 2;
            semanticAnalyze(symbolTable, belong + "_BlockItemNo" + index, (BlockItem) comps.get(index),
                    isInFor, isInVoid, isIntFunc, isLast, ((WordDescription) comps.get(len - 1)).getLine(),
                    changeLabel, endForLabel);
        }
        if (len - 1 == 1 && isIntFunc) {
            errorDescriptions.add(new ErrorDescription(((WordDescription) comps.get(len - 1)).getLine(), ErrorType.g));
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, BlockItem blockItem,
                                 boolean isInFor, boolean isInVoid, boolean isIntFunc, boolean isLast, int rBraceLine,
                                 String changeLabel, String endForLabel) {
        ArrayList<SyntaxComp> comps = blockItem.getComps();
        String platform = belong + "_BlockItem";
        if (comps.get(0) instanceof Decl) {
            semanticAnalyze(symbolTable, platform, (Decl) comps.get(0), false);
            if (isLast && isIntFunc) {
                errorDescriptions.add(new ErrorDescription(rBraceLine, ErrorType.g));
            }
        } else {
            stmtSemanticAnalyze(symbolTable, platform, (Stmt) comps.get(0), isInFor, isInVoid,
                    isIntFunc, isLast, rBraceLine,
                    changeLabel, endForLabel);
        }
    }

    private void stmtSemanticAnalyze(SymbolTable symbolTable, String belong, Stmt stmt,
                                     boolean isInFor, boolean isInVoid,
                                     boolean isIntFunc, boolean isLast, int rBraceLine,
                                     String changeLabel, String stmtEndLabel) {
        //todo
        String platform = belong + "_Stmt";
        ArrayList<SyntaxComp> comps = stmt.getComps();
        SyntaxComp first = comps.get(0);
        String retLabel = "";
        boolean returnType = false;
        if (first instanceof Lval) {
            String ident = ((Ident) first.getComps().get(0)).getSyntaxContent().getContent();
            SymbolPara para = symbolTable.findPara(ident);
            if (para instanceof FuncPara) {
                errorDescriptions.add(new ErrorDescription(((Lval) first).getLine(), ErrorType.c));
                return;
            }
            VarPara varPara = (VarPara) para;
            if (varPara == null) {
                errorDescriptions.add(new ErrorDescription(((Lval) first).getLine(), ErrorType.c));
                return;
            }
            boolean isConst = varPara.getIsConst();
            if (isConst) {
                errorDescriptions.add(new ErrorDescription(((Lval) first).getLine(), ErrorType.h));
                return;
            }
            boolean isGlobal = varPara.getIsGlobal();
            int dimen = varPara.getDimen();
            ExpReturn lvalReturn = semanticAnalyze(symbolTable, platform, (Lval) first, false, false);
            String lvalLabel = lvalReturn.getContent();
            if (comps.get(2) instanceof Exp) {
                ExpReturn expReturn = semanticAnalyze(symbolTable, platform, (Exp) comps.get(2), false);
                boolean isValue = expReturn.isValue();
                int value = 0;
                if (isValue) {
                    value = expReturn.getValue();
                } else {
                    retLabel = expReturn.getContent();
                }
                if (isValue) {
                    if (isGlobal && dimen == 0) {
                        instructions.add(new Store(value, true, ident, true));
                    } else {
                        instructions.add(new Store(value, true, lvalLabel, false));
                    }
                } else if (!isGlobal || dimen > 0) {
                    instructions.add(new Store(retLabel, false, lvalLabel, false));
                } else {
                    instructions.add(new Store(retLabel, false, ident, true));
                }
            } else {
                String getLabel = platform + "_Getint";
                instructions.add(new GetInt(getLabel));
                if (!isGlobal || dimen > 0) {
                    instructions.add(new Store(getLabel, false, lvalLabel, false));
                } else {
                    instructions.add(new Store(getLabel, false, ident, true));
                }
            }
        } else if (first instanceof Block) {
            SymbolTable symbolTableTmp = new SymbolTable(symbolTable);
            semanticAnalyze(symbolTableTmp, platform, (Block) first, isInFor, isInVoid, false,
                    changeLabel, stmtEndLabel);
        } else if (first instanceof Exp) {
            semanticAnalyze(symbolTable, platform, (Exp) first, false);
        } else {
            WordTypeCode first2WdCode = ((WordDescription) first).getWordTypeCode();
            switch (first2WdCode) {
                case IFTK -> {
                    ifNumber += 1;
                    String stmtBeginName = "if_" + ifNumber + "_stmt_begin";
                    String stmtEndName = "if_" + ifNumber + "_stmt_end";
                    String ifEndName = "if_" + ifNumber + "_end";
                    Cond cond = (Cond) comps.get(2);
                    SymbolTable symbolTableNew = new SymbolTable(symbolTable);
                    if (comps.size() > 5) {
                        semanticAnalyze(symbolTableNew, platform, cond, stmtBeginName, stmtEndName);
                        instructions.add(new Label(stmtBeginName));
                        stmtSemanticAnalyze(symbolTableNew, platform + "_If", (Stmt) comps.get(4),
                                isInFor, isInVoid, false, false, rBraceLine, changeLabel, stmtEndLabel);
                        instructions.add(new Br(ifEndName));
                        instructions.add(new Label(stmtEndName));
                        stmtSemanticAnalyze(symbolTableNew, platform + "_Else", (Stmt) comps.get(6),
                                isInFor, isInVoid, false, false, rBraceLine, changeLabel, stmtEndLabel);
                        instructions.add(new Br(ifEndName));
                    } else {
                        semanticAnalyze(symbolTableNew, platform, cond, stmtBeginName, ifEndName);
                        instructions.add(new Label(stmtBeginName));
                        stmtSemanticAnalyze(symbolTableNew, platform + "_If", (Stmt) comps.get(4),
                                isInFor, isInVoid, false, false, rBraceLine, changeLabel, stmtEndLabel);
                        instructions.add(new Br(ifEndName));
                    }
                    instructions.add(new Label(ifEndName));
                }
                case FORTK -> {
                    forNumber += 1;
                    SymbolTable symbolTableNew = new SymbolTable(symbolTable);
                    int index = 2;
                    if (comps.get(index) instanceof ForStmt forStmt) {
                        semanticAnalyze(symbolTableNew, platform + "_ForStmt1", forStmt);
                        index += 1;
                    }
                    index += 1;
                    Cond cond = null;
                    if (comps.get(index) instanceof Cond cond_in) {
                        cond = cond_in;
                        index += 1;
                    }
                    index += 1;
                    ForStmt forStmt2 = null;
                    if (comps.get(index) instanceof ForStmt forStmt2_in) {
                        forStmt2 = forStmt2_in;
                        index += 1;
                    }
                    index += 1;
                    String forBeginLabelName = "for_" + forNumber + "_begin";
                    instructions.add(new Br(forBeginLabelName));    //起到声明的作用
                    instructions.add(new Label(forBeginLabelName));
                    String stmtBeginName = "for_" + forNumber + "_stmt_begin";
                    String stmtEndName = "for_" + forNumber + "_stmt_end";
                    String changeName = "for_" + forNumber + "_varChange";
                    if (cond != null) {
                        semanticAnalyze(symbolTableNew, platform, cond, stmtBeginName, stmtEndName);
                    } else {
                        instructions.add(new Br(stmtBeginName));
                    }
                    instructions.add(new Label(stmtBeginName));
                    stmtSemanticAnalyze(symbolTableNew, platform + "_StmtInFor", (Stmt) comps.get(index),
                            true, isInVoid, false, false, rBraceLine, changeName, stmtEndName);
                    instructions.add(new Br(changeName));
                    instructions.add(new Label(changeName));
                    if (forStmt2 != null) {
                        semanticAnalyze(symbolTable, platform + "_ForStmt2", forStmt2);
                    }
                    instructions.add(new Br(forBeginLabelName));
                    instructions.add(new Label(stmtEndName));
                }
                case BREAKTK -> {
                    if (!isInFor) {
                        errorDescriptions.add(new ErrorDescription(((WordDescription) first).getLine(), ErrorType.m));
                    } else {
                        instructions.add(new Br(stmtEndLabel));
                    }
                }
                case CONTINUETK -> {
                    if (!isInFor) {
                        errorDescriptions.add(new ErrorDescription(((WordDescription) first).getLine(), ErrorType.m));
                    } else {
                        instructions.add(new Br(changeLabel));
                    }
                }
                case RETURNTK -> {
                    if (comps.get(1) instanceof Exp) {
                        returnType = true;
                        ExpReturn returnExpReturn = semanticAnalyze(symbolTable, platform, (Exp) comps.get(1), false
                        );
                        boolean isValue = returnExpReturn.isValue();
                        if (isInVoid) {
                            errorDescriptions.add(new ErrorDescription(((WordDescription) first).getLine(),
                                    ErrorType.f));
                        } else {
                            instructions.add(new FuncRet(WordTypeCode.INTTK, returnExpReturn.getContent(), isValue));
                        }
                    } else {
                        instructions.add(new FuncRet(WordTypeCode.VOIDTK, -1, false));
                    }
                }
                case PRINTFTK -> {
                    FormatString formatString = (FormatString) comps.get(2);
                    String format = formatString.getSyntaxContent().getContent();
                    boolean wrong = false;
                    int lastCharType = 0;
                    int countParams = 0;
                    for (char c : format.toCharArray()) {
                        if (c == '"') {
                            continue;
                        }
                        if (!(c == '%' || c == 32 || c == 33 || (c >= 40 && c <= 126))) {
                            wrong = true;
                        } else {
                            if (lastCharType == 1) {    // %
                                if (c != 'd') {
                                    wrong = true;
                                } else {
                                    countParams++;
                                }
                            } else if (lastCharType == 2) {     // \
                                if (c != 'n') {
                                    wrong = true;
                                }
                            }
                        }
                        if (c == '%') {
                            lastCharType = 1;
                        } else if (c == '\\') {
                            lastCharType = 2;
                        } else {
                            lastCharType = 0;
                        }
                    }
                    if (lastCharType == 1 || lastCharType == 2) {    // %
                        wrong = true;
                    }
                    if (wrong) {
                        errorDescriptions.add(new ErrorDescription(formatString.getLine(), ErrorType.a));
                    }
                    int givenParams = 0;
                    format = format.substring(1, format.length() - 1);
                    int index = 4;
                    int len = comps.size();
                    ArrayList<Exp> exps = new ArrayList<>();
                    while (index < len && comps.get(index) instanceof Exp) {
                        exps.add((Exp) comps.get(index));
                        index += 2;
                        givenParams += 1;
                    }
                    if (countParams != givenParams) {
                        errorDescriptions.add(new ErrorDescription(((WordDescription) first).getLine(), ErrorType.l));
                        return;
                    }
                    if (!wrong) {
                        output(symbolTable, platform, format, exps);
                    }
                }
            }
        }
        if (isIntFunc && isLast && !returnType) {
            errorDescriptions.add(new ErrorDescription(rBraceLine, ErrorType.g));
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, ForStmt forStmt) {
        String platform = belong + "_ForStmt";
        ArrayList<SyntaxComp> comps = forStmt.getComps();
        Lval first = (Lval) comps.get(0);
        String ident = ((Ident) first.getComps().get(0)).getSyntaxContent().getContent();
        VarPara varPara = symbolTable.findVarPara(ident);
        boolean isGlobal = varPara.getIsGlobal();
        int dimen = varPara.getDimen();
        ExpReturn lvalReturn = semanticAnalyze(symbolTable, platform, first, false, false);
        String lvalLabel = lvalReturn.getContent();
        ExpReturn expReturn = semanticAnalyze(symbolTable, platform, (Exp) comps.get(2), false);
        boolean isValue = expReturn.isValue();
        String expLabel = "";
        int value = 0;
        if (isValue) {
            value = expReturn.getValue();
        } else {
            expLabel = expReturn.getContent();
        }
        if (isValue) {
            if (isGlobal && dimen == 0) {
                instructions.add(new Store(value, true, ident, true));
            } else {
                instructions.add(new Store(value, true, lvalLabel, false));
            }
        } else if (!isGlobal || dimen > 0) {
            instructions.add(new Store(expLabel, false, lvalLabel, false));
        } else {
            instructions.add(new Store(expLabel, false, ident, true));
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, Cond cond,
                                 String trueLabelName, String falseLabelName) {
        String platform = belong + "_Cond";
        semanticAnalyze(symbolTable, platform, (LOrExp) cond.getComps().get(0), trueLabelName,
                falseLabelName);
    }

    private void semanticAnalyze(SymbolTable symbolTable, String belong, LOrExp lOrExp,
                                 String trueLabelName, String falseLabelName) {
        //最后一句也需br
        String platform = belong + "_LOrExp";
        lOrExpNumber += 1;
        int andExpNum = 0;
        int lAndSub = 0;
        ArrayList<SyntaxComp> comps = lOrExp.getComps();
        int len = comps.size();
        lAndSub += 1;
        String nextLabelName = "LOrExp_" + lOrExpNumber + "_comp_" + (andExpNum + 2);
        ExpReturn expReturn = semanticAnalyze(symbolTable, platform + "_LAndExp" + lAndSub, (LAndExp) comps.get(0),
                nextLabelName);
        int value = 0;
        boolean isValue = expReturn.isValue();
        String expLabel = "";
        if (isValue) {
            value = expReturn.getValue();
            assert value == 0 || value == 1;
            if (value == 1) {
                andExpNum += 1;
            }
        } else {
            expLabel = expReturn.getContent();
            instructions.add(new Br(expLabel, trueLabelName, nextLabelName));
            instructions.add(new Label(nextLabelName));
            andExpNum += 1;
        }
        int index = 1;
        String lastLabel;
        boolean lastIsValue;
        if (!(isValue && value == 1)) {//第一个不是定值1
            while (index + 1 < len) {
                lastLabel = expLabel;
                lastIsValue = isValue;
                lAndSub += 1;
                nextLabelName = "LOrExp_" + lOrExpNumber + "_comp_" + (andExpNum + 2);
                ExpReturn expReturn_tmp = semanticAnalyze(symbolTable, platform + "_LAndExp" + lAndSub,
                        (LAndExp) comps.get(index + 1), nextLabelName);
                isValue = expReturn_tmp.isValue();
                expLabel = expReturn_tmp.getContent();
                if (isValue) {
                    value = expReturn_tmp.getValue();
                    assert value == 0 || value == 1;
                    if (value == 0) {
                        instructions.add(new Br(nextLabelName));
                        instructions.add(new Label(nextLabelName));
                        isValue = lastIsValue;
                        expLabel = lastLabel;
                        andExpNum += 1;
                    } else {
                        break;
                    }
                } else {
                    instructions.add(new Br(expLabel, trueLabelName, nextLabelName));
                    instructions.add(new Label(nextLabelName));
                    andExpNum += 1;
                }
                index += 2;
            }
        }
        if (!isValue) {
            instructions.add(new Br(falseLabelName));
        } else {
            if (value == 1) {
                instructions.add(new Br(trueLabelName));
            } else {
                instructions.add(new Br(falseLabelName));
            }
        }
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, LAndExp lAndExp, String falseLabelName) {
        //最后一句无需br
        String platform = belong + "_LAndExp";
        lAndExpNumber += 1;
        int eqExpSub = 0;
        int eqExpNum = 0;
        ArrayList<SyntaxComp> comps = lAndExp.getComps();
        int len = comps.size();
        eqExpSub += 1;
        ExpReturn expReturn = semanticAnalyze(symbolTable, platform + "_EqExp" + eqExpSub, (EqExp) comps.get(0));
        int value = 0;
        boolean isValue = expReturn.isValue();
        String expLabel = "";
        boolean skip = false;
        if (isValue) {
            value = expReturn.getValue();
            assert value == 0 || value == 1;
            if (value == 0) {
                eqExpNum += 1;
                skip = true;
            }
        } else {
            eqExpNum += 1;
            expLabel = expReturn.getContent();
        }
        int index = 1;
        String lastLabel;
        boolean lastIsValue;
        if (!(isValue && value == 0)) {//第一个不是定值0
            while (index + 1 < len) {
                lastLabel = expLabel;
                lastIsValue = isValue;
                eqExpSub += 1;
                if (eqExpNum > 0 && !skip) {
                    String nextLabelName = "LAndExp_" + lAndExpNumber + "_comp_" + (eqExpNum + 1);
                    instructions.add(new Br(lastLabel, nextLabelName, falseLabelName));
                    instructions.add(new Label(nextLabelName));
                }
                skip = false;
                ExpReturn expReturn_tmp = semanticAnalyze(symbolTable, platform + "_EqExp" + eqExpSub,
                        (EqExp) comps.get(index + 1));
                isValue = expReturn_tmp.isValue();
                expLabel = expReturn_tmp.getContent();
                if (isValue) {
                    value = expReturn_tmp.getValue();
                    assert value == 0 || value == 1;
                    if (value == 0) {
                        break; //读到0了，不用往后读了
                    } else {
                        isValue = lastIsValue;
                        expLabel = lastLabel;
                        skip = true;
                    }
                } else {
                    eqExpNum += 1;
                }
                index += 2;
            }
        }
        if (!isValue) {
            return new ExpReturn(false, expLabel);
        } else {
            return new ExpReturn(value);
        }
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, EqExp eqExp) {
        //确保返回的insIndex,%insIndex为i1,value也为i1
        //最后一句无需br
        String platform = belong + "_EqExp";
        ArrayList<SyntaxComp> comps = eqExp.getComps();
        int relExpNumber = 0;
        int len = comps.size();
        boolean allValue;
        relExpNumber += 1;
        ExpReturn expReturn = semanticAnalyze(symbolTable, platform + "_RelExp" + relExpNumber, (RelExp) comps.get(0));
        int value = 0;
        boolean isOne = (comps.get(0).getComps().size() == 1);
        boolean isValue = expReturn.isValue();
        String expLabel = "";
        allValue = isValue;
        if (isValue) {
            value = expReturn.getValue();
        } else {
            expLabel = expReturn.getContent();
        }
        int index = 1;
        int lastValue;
        String lastLabel;
        boolean lastIsOne;
        boolean lastIsValue;
        int cmpNumber = 0;
        int zextNumber = 0;
        while (index + 1 < len) {
            lastValue = value;
            lastLabel = expLabel;
            lastIsOne = isOne;
            lastIsValue = allValue;
            relExpNumber += 1;
            ExpReturn expReturn_tmp = semanticAnalyze(symbolTable, platform + "_RelExp" + relExpNumber,
                    (RelExp) comps.get(index + 1));
            isValue = expReturn_tmp.isValue();
            isOne = (comps.get(index + 1).getComps().size() == 1);
            allValue &= isValue;
            if (isValue) {
                value = expReturn_tmp.getValue();
            } else {
                expLabel = expReturn_tmp.getContent();
            }
            WordTypeCode wordTypeCode = ((WordDescription) comps.get(index)).getWordTypeCode();
            assert wordTypeCode == WordTypeCode.EQL || wordTypeCode == WordTypeCode.NEQ;
            if (allValue) {
                if (wordTypeCode == WordTypeCode.EQL) {
                    value = (lastValue == value) ? 1 : 0;
                } else {
                    value = (lastValue == value) ? 0 : 1;
                }
            } else {
                String insOp1;
                if (lastIsValue) {
                    insOp1 = String.valueOf(lastValue);
                } else {
                    zextNumber += 1;
                    String zextLabel = platform + "_Zext" + zextNumber;
                    if (!lastIsOne) {
                        instructions.add(new Zext(zextLabel, lastLabel));
                        insOp1 = zextLabel;
                    } else {
                        insOp1 = lastLabel;
                    }
                }
                String insOp2;
                if (isValue) {
                    insOp2 = String.valueOf(value);
                } else {
                    zextNumber += 1;
                    String zextLabel = platform + "_Zext" + zextNumber;
                    if (!isOne) {
                        instructions.add(new Zext(zextLabel, expLabel));
                        insOp2 = zextLabel;
                    } else {
                        insOp2 = expLabel;
                    }
                }
                CmpType cmpType;
                if (wordTypeCode == WordTypeCode.EQL) {
                    cmpType = CmpType.eq;
                } else {
                    cmpType = CmpType.ne;
                }
                cmpNumber += 1;
                String cmpLabel = platform + "_Cmp" + cmpNumber;
                instructions.add(new Compare(cmpType, cmpLabel,
                        insOp1, lastIsValue, insOp2, isValue));
                expLabel = cmpLabel;
                isOne = false;
            }
            index += 2;
        }
        if (!allValue) {
            if (len == 1 && isOne) {//代表是单值
                String resLabel = platform + "_TransCmp";
                instructions.add(new Compare(CmpType.ne, resLabel,
                        expLabel, false, String.valueOf(0), true));
                expLabel = resLabel;
            }
            return new ExpReturn(false, expLabel);
        } else {
            if (len == 1 && isOne) {//代表是单值
                value = (value != 0) ? 1 : 0;
            }
            return new ExpReturn(value);
        }
    }

    private ExpReturn semanticAnalyze(SymbolTable symbolTable, String belong, RelExp relExp) {
        //确保返回的insIndex,%insIndex为i1
        //最后一句无需br
        String platform = belong + "_RelExp";
        ArrayList<SyntaxComp> comps = relExp.getComps();
        int len = comps.size();
        boolean allValue;
        int addExpNumber = 0;
        addExpNumber += 1;
        ExpReturn expReturn = semanticAnalyze(symbolTable, platform + "_AddExp" + addExpNumber, (AddExp) comps.get(0),
                false);
        int value = 0;
        String expLabel = "";
        boolean isValue = expReturn.isValue();
        allValue = isValue;
        if (isValue) {
            value = expReturn.getValue();
        } else {
            expLabel = expReturn.getContent();
        }
        int zextNumber = 0;
        int cmpNumber = 0;
        int index = 1;
        int lastValue;
        String lastLabel;
        boolean lastIsValue;
        boolean isOne = true;
        while (index + 1 < len) {
            lastValue = value;
            lastLabel = expLabel;
            lastIsValue = allValue;
            addExpNumber += 1;
            ExpReturn expReturn_tmp = semanticAnalyze(symbolTable, platform + "_AddExp" + addExpNumber,
                    (AddExp) comps.get(index + 1), false);
            isValue = expReturn_tmp.isValue();
            allValue &= isValue;
            if (isValue) {
                value = expReturn_tmp.getValue();
            } else {
                expLabel = expReturn_tmp.getContent();
            }
            WordTypeCode wordTypeCode = ((WordDescription) comps.get(index)).getWordTypeCode();
            assert wordTypeCode == WordTypeCode.LSS || wordTypeCode == WordTypeCode.LEQ
                    || wordTypeCode == WordTypeCode.GRE || wordTypeCode == WordTypeCode.GEQ;
            if (allValue) {
                value = switch (wordTypeCode) {
                    case LSS -> (lastValue < value) ? 1 : 0;
                    case LEQ -> (lastValue <= value) ? 1 : 0;
                    case GRE -> (lastValue > value) ? 1 : 0;
                    default -> (lastValue >= value) ? 1 : 0;
                };
            } else {
                String insOp1;
                if (lastIsValue) {
                    insOp1 = String.valueOf(lastValue);
                } else {
                    zextNumber += 1;
                    String zextLabel = platform + "_Zext" + zextNumber;
                    if (!isOne) {
                        instructions.add(new Zext(zextLabel, lastLabel));
                        insOp1 = zextLabel;
                    } else {
                        insOp1 = lastLabel;
                    }
                }
                String insOp2 = (isValue) ? String.valueOf(value) : expLabel;
                CmpType cmpType = switch (wordTypeCode) {
                    case LSS -> CmpType.slt;
                    case LEQ -> CmpType.sle;
                    case GRE -> CmpType.sgt;
                    default -> CmpType.sge;
                };
                cmpNumber += 1;
                String cmpLabel = platform + "_Cmp" + cmpNumber;
                instructions.add(new Compare(cmpType, cmpLabel,
                        insOp1, lastIsValue, insOp2, isValue));
                expLabel = cmpLabel;
                isOne = false;
            }
            index += 2;
        }
        if (!allValue) {
            return new ExpReturn(false, expLabel);
        } else {
            return new ExpReturn(value);
        }
    }

    private void output(SymbolTable symbolTable, String belong, String format, ArrayList<Exp> exps) {
        String platform = belong + "_Output";
        int len = format.length();
        int stringIndex = 0;
        int expIndex = 0;
        int paramNumber = 0;
        while (stringIndex < len) {
            paramNumber += 1;
            char c = format.charAt(stringIndex);
            switch (c) {
                case '\\' -> {
                    instructions.add(new PutCh((char) 10));
                    stringIndex += 2;
                }
                case '%' -> {
                    ExpReturn expReturn = semanticAnalyze(symbolTable, platform + "_Param" + paramNumber,
                            exps.get(expIndex), false
                    );
                    instructions.add(new PutInt(expReturn.getContent(), expReturn.isValue()));
                    expIndex += 1;
                    stringIndex += 2;
                }
                default -> {
                    instructions.add(new PutCh(c));
                    stringIndex += 1;
                }
            }
        }
    }

    private void semanticAnalyze(SymbolTable symbolTable, MainFuncDef mainFuncDef) {
        ArrayList<SyntaxComp> comps = mainFuncDef.getComps();
        SymbolTable symbolTableNew = new SymbolTable(symbolTable);
        ArrayList<ParamInfo> paramInfos = new ArrayList<>();
        String identName = "main";
        String platform = "CompUnit_MainFuncDef_main";
        symbolTable.addSymbol("main", new FuncPara(WordTypeCode.INTTK, paramInfos, mainFuncDef.isLackRParent()));
        instructions.add(new FuncDefBegin(WordTypeCode.INTTK, identName, paramInfos));
        semanticAnalyze(symbolTableNew, platform, (Block) comps.get(4),
                false, false, true, null, null);
        instructions.add(new FuncDefEnd());
    }
}
