package codeGen;

import codeGen.mipsIns.MipsIns;
import codeGen.mipsIns.calculate.calcuIns.Addu;
import codeGen.mipsIns.calculate.calcuIns.Div;
import codeGen.mipsIns.calculate.calcuIns.Mult;
import codeGen.mipsIns.calculate.calcuIns.Or;
import codeGen.mipsIns.calculate.calcuIns.Subu;
import codeGen.mipsIns.calculate.calcuIns.Xor;
import codeGen.mipsIns.calculate.hiLo.Mfhi;
import codeGen.mipsIns.calculate.hiLo.Mflo;
import codeGen.mipsIns.compare.Slt;
import codeGen.mipsIns.copy.Copy;
import codeGen.mipsIns.jump.Beqz;
import codeGen.mipsIns.jump.Jal;
import codeGen.mipsIns.jump.Jr;
import codeGen.mipsIns.jump.JumpLabel;
import codeGen.mipsIns.label.MipsLabel;
import codeGen.mipsIns.loadStore.Lw;
import codeGen.mipsIns.loadStore.Sw;
import codeGen.mipsIns.seg.data.DataBegin;
import codeGen.mipsIns.seg.data.DataVar;
import codeGen.mipsIns.seg.text.TextBegin;
import codeGen.mipsIns.setValue.SetValue;
import codeGen.mipsIns.stack.StackPushByte;
import codeGen.mipsIns.syscall.Syscall;
import codeGen.symbolTable.MipsSymbolTable;
import codeGen.symbolTable.symbol.MipsGlobalVar;
import codeGen.symbolTable.symbol.MipsTempVar;
import codeGen.symbolTable.symbol.MipsVar;
import lexer.WordTypeCode;
import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.LLVMIR.alloc.AllocVar;
import semanticAnalyzer.LLVMIR.array.GetArrayElePtr;
import semanticAnalyzer.LLVMIR.bitExt.Zext;
import semanticAnalyzer.LLVMIR.calculate.CalcuType;
import semanticAnalyzer.LLVMIR.calculate.Calculate;
import semanticAnalyzer.LLVMIR.compare.CmpType;
import semanticAnalyzer.LLVMIR.compare.Compare;
import semanticAnalyzer.LLVMIR.func.funcCall.FuncCall;
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
import semanticAnalyzer.symbolTable.symbolPara.ParamInfo;

import java.util.ArrayList;

public class CodeGen {
    private final ArrayList<LLVMIRIns> llvmirIns;
    private final ArrayList<MipsIns> mipsIns;
    private final MipsSymbolTable mipsSymbolTable;

    public CodeGen(ArrayList<LLVMIRIns> llvmirIns) {
        this.llvmirIns = llvmirIns;
        this.mipsIns = new ArrayList<>();
        this.mipsSymbolTable = new MipsSymbolTable(null);
    }

    public ArrayList<MipsIns> genMips() {
        mipsIns.add(new DataBegin());
        int dataEnd = 2;
        int dataAddr = 0x10010000;
        for (int i = dataEnd + 1; i < llvmirIns.size(); i++) {
            if (llvmirIns.get(i) instanceof GlobalVar globalVar) {
                String ident = globalVar.getName();
                int dimen = globalVar.getDimen();
                int[] dimens = globalVar.getDimens();
                int[][] value = globalVar.getValue();   //可能为null，代表全0
                mipsIns.add(new DataVar(ident, dimen, dimens, value));
                mipsSymbolTable.addSymbol(ident, new MipsGlobalVar(dataAddr));
                dataAddr += 4 * switch (dimen) {
                    case 0 -> 1;
                    case 1 -> dimens[0];
                    default -> dimens[0] * dimens[1];
                };
                dataEnd = i;
            }
        }
        mipsIns.add(new TextBegin());
        //在调用putch,putint时直接编写代码即可
        mipsIns.add(new SetValue("fp", dataAddr));
        mipsIns.add(new JumpLabel("main"));
        for (int i = dataEnd + 1; i < llvmirIns.size(); i++) {
            if (llvmirIns.get(i) instanceof FuncDefBegin funcDefBegin) {
                int funcWordSize = 0;
                String name = funcDefBegin.getName();
                boolean isMain = name.equals("main");
                mipsIns.add(new MipsLabel(name));
                MipsSymbolTable mipsSymbolTableNew = new MipsSymbolTable(mipsSymbolTable);
                if (!isMain) {
                    protectRa();
                }
                ArrayList<ParamInfo> paramInfos = funcDefBegin.getParamInfos();
                for (ParamInfo paramInfo : paramInfos) {
                    boolean isPtr = paramInfo.getDimen() > 0;
                    int dimen = paramInfo.getDimen();
                    mipsSymbolTableNew.addSymbol(paramInfo.getOutName(), new MipsTempVar(funcWordSize,
                            (isPtr) ? (dimen - 1) : dimen, isPtr, false));
                    funcWordSize += 1;
                }
                i += 1;
                while (!(llvmirIns.get(i) instanceof FuncDefEnd)) {
                    switch (llvmirIns.get(i).getLlvmirInsType()) {
                        case allocVar ->
                                funcWordSize = allocVar((AllocVar) llvmirIns.get(i), mipsSymbolTableNew, funcWordSize);
                        case getArrayElePtr ->
                                funcWordSize = getArrayEle((GetArrayElePtr) llvmirIns.get(i), mipsSymbolTableNew,
                                        funcWordSize);
                        case calculate -> funcWordSize = calculate((Calculate) llvmirIns.get(i), mipsSymbolTableNew,
                                funcWordSize);
                        case compare ->
                                funcWordSize = compare((Compare) llvmirIns.get(i), mipsSymbolTableNew, funcWordSize);
                        case funcCall ->
                                funcWordSize = funcCall((FuncCall) llvmirIns.get(i), mipsSymbolTableNew, funcWordSize);
                        case funcRet -> funcReturn((FuncRet) llvmirIns.get(i), mipsSymbolTableNew, isMain);
                        case getInt ->
                                funcWordSize = getInt((GetInt) llvmirIns.get(i), mipsSymbolTableNew, funcWordSize);
                        case br -> br((Br) llvmirIns.get(i), mipsSymbolTableNew);
                        case label -> mipsIns.add(new MipsLabel(((Label) llvmirIns.get(i)).getLabelName()));
                        case load -> funcWordSize = load((Load) llvmirIns.get(i), mipsSymbolTableNew, funcWordSize);
                        case store -> store((Store) llvmirIns.get(i), mipsSymbolTableNew);
                        case putCh -> putCh((PutCh) llvmirIns.get(i));
                        case putInt -> putInt((PutInt) llvmirIns.get(i), mipsSymbolTableNew);
                        case zext -> funcWordSize = zext((Zext) llvmirIns.get(i), mipsSymbolTableNew, funcWordSize);
                        //funcbegin, funcend, funcdec忽略
                    }
                    i += 1;
                }
            }
        }
        return mipsIns;
    }

    private int allocVar(AllocVar allocVar, MipsSymbolTable mipsSymbolTableNew, int funcWordSize) {
        int nowFuncWordSize = funcWordSize;
        String varName = allocVar.getResName();
        int dimen = allocVar.getDimen();
        int[] dimens = allocVar.getDimens();
        boolean isArrPtr = allocVar.isArrPtr();
        if (isArrPtr) {
            mipsSymbolTableNew.addSymbol(varName, new MipsTempVar(nowFuncWordSize, dimen - 1, true, true));
            mipsIns.add(new SetValue("t1", 4 * (nowFuncWordSize + 1)));
            mipsIns.add(new Addu("t1", "fp", "t1"));
            mipsIns.add(new Sw("t1", "fp", 4 * nowFuncWordSize));
            nowFuncWordSize += 2;
        } else {
            int numbers = switch (dimen) {
                case 0 -> 1;
                case 1 -> dimens[0];
                default -> dimens[0] * dimens[1];
            };
            mipsSymbolTableNew.addSymbol(varName, new MipsTempVar(nowFuncWordSize, dimen, true, false));
            mipsIns.add(new SetValue("t1", 4 * (nowFuncWordSize + 1)));
            mipsIns.add(new Addu("t1", "fp", "t1"));
            mipsIns.add(new Sw("t1", "fp", 4 * nowFuncWordSize));
            nowFuncWordSize += numbers + 1;
        }
        return nowFuncWordSize;
    }

    private int zext(Zext zext, MipsSymbolTable mipsSymbolTableNew, int funcWordSize) {
        int nowFuncWordSize = funcWordSize;
        String op = zext.getOp();
        int opWordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(op)).getWordNumberToFp();
        mipsIns.add(new Lw("t0", "fp", 4 * opWordNumber));
        String resName = zext.getResIndex();
        mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize));
        int resWordNumber = nowFuncWordSize;
        nowFuncWordSize += 1;
        mipsIns.add(new Sw("t0", "fp", 4 * resWordNumber));
        return nowFuncWordSize;
    }

    private void store(Store store, MipsSymbolTable mipsSymbolTableNew) {
        String sourceName = store.getSource();
        String destName = store.getDest();
        boolean sourceIsValue = store.isValue();
        if (sourceIsValue) {
            mipsIns.add(new SetValue("t0", Integer.parseInt(sourceName)));
        } else {
            MipsTempVar sourceSymbol = (MipsTempVar) mipsSymbolTableNew.getSymbol(sourceName);
            int sourceWordNumber = sourceSymbol.getWordNumberToFp();
            mipsIns.add(new Lw("t0", "fp", 4 * sourceWordNumber));
        }
        MipsVar destVar = mipsSymbolTableNew.getSymbol(destName);
        if (destVar instanceof MipsGlobalVar) {
            int destAddr = ((MipsGlobalVar) destVar).getAddress();
            mipsIns.add(new Sw("t0", destAddr));
        } else {
            int destWordNumber = ((MipsTempVar) destVar).getWordNumberToFp();
            mipsIns.add(new Lw("t1", "fp", 4 * destWordNumber));
            mipsIns.add(new Sw("t0", "t1", 0));
        }
    }

    private int load(Load load, MipsSymbolTable mipsSymbolTableNew, int funcWordSize) {
        int nowFuncWordSize = funcWordSize;
        String resName = load.getResIndex();
        String source = load.getSource();
        MipsVar sourceSymbol = mipsSymbolTableNew.getSymbol(source);
        if (sourceSymbol instanceof MipsGlobalVar) {
            int sourceAddr = ((MipsGlobalVar) sourceSymbol).getAddress();
            mipsIns.add(new Lw("t0", sourceAddr));
            mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize));
        } else {
            int sourceWordNumber = ((MipsTempVar) sourceSymbol).getWordNumberToFp();
            mipsIns.add(new Lw("t0", "fp", 4 * sourceWordNumber));
            mipsIns.add(new Lw("t0", "t0", 0));
            boolean isPtr = ((MipsTempVar) sourceSymbol).isPtr();
            assert isPtr;
            boolean isPtrPtr = ((MipsTempVar) sourceSymbol).isPtrPtr();
            int dimen = ((MipsTempVar) sourceSymbol).getDimen();
            if (isPtrPtr) {
                mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize, dimen, true, false));
            } else {
                mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize, dimen, false, false));
            }
        }
        int resWordNumber = nowFuncWordSize;
        nowFuncWordSize += 1;
        mipsIns.add(new Sw("t0", "fp", 4 * resWordNumber));
        return nowFuncWordSize;
    }

    private int funcCall(FuncCall funcCall, MipsSymbolTable mipsSymbolTableNew, int funcWordSize) {
        int nowFuncWordSize = funcWordSize;
        boolean isVoid = funcCall.isVoid();
        int paramNumber = funcCall.getParamInfosNumber();
        ArrayList<String> paramString = funcCall.getParamIndex();
        ArrayList<Boolean> paramIsValue = funcCall.getParamIsValue();
        mipsIns.add(new Sw("fp", "sp", 0));
        mipsIns.add(new StackPushByte(1));
        mipsIns.add(new SetValue("t0", 4 * nowFuncWordSize));
        mipsIns.add(new Addu("t1", "fp", "t0"));
        for (int j = 0; j < paramNumber; j++) {
            if (paramIsValue.get(j)) {
                mipsIns.add(new SetValue("t0", Integer.parseInt(paramString.get(j))));
            } else {
                String paramName = paramString.get(j);
                int paramWordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(paramName)).getWordNumberToFp();
                mipsIns.add(new Lw("t0", "fp", 4 * paramWordNumber));
//                mipsIns.add(new SetValue("t2", 4 * paramWordNumber));
//                mipsIns.add(new Addu("t0", "fp", "t2"));
            }
            mipsIns.add(new Sw("t0", "t1", 4 * j));
        }
        mipsIns.add(new Copy("fp", "t1"));
        mipsIns.add(new Jal(funcCall.getFuncName()));
        mipsIns.add(new StackPushByte(-1));
        mipsIns.add(new Lw("fp", "sp", 0));
        if (!isVoid) {
            String resName = funcCall.getResIndex();
            mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize));
            int resWordNumber = nowFuncWordSize;
            nowFuncWordSize += 1;
            mipsIns.add(new Sw("v0", "fp", 4 * resWordNumber));
        }
        return nowFuncWordSize;
    }

    private void putInt(PutInt putInt, MipsSymbolTable mipsSymbolTableNew) {
        boolean isValue = putInt.isValue();
        String index = putInt.getIndex();
        if (isValue) {
            mipsIns.add(new SetValue("a0", Integer.parseInt(index)));
        } else {
            int indexWordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(index)).getWordNumberToFp();
            mipsIns.add(new Lw("a0", "fp", 4 * indexWordNumber));
        }
        mipsIns.add(new SetValue("v0", 1));
        mipsIns.add(new Syscall());
    }

    private void putCh(PutCh putCh) {
        char ch = putCh.getCh();
        mipsIns.add(new SetValue("a0", ch));
        mipsIns.add(new SetValue("v0", 11));
        mipsIns.add(new Syscall());
    }

    private void br(Br br, MipsSymbolTable mipsSymbolTableNew) {
        boolean isNoCond = br.isNoCond();
        String trueLabel = br.getIfTrue();
        if (isNoCond) {
            mipsIns.add(new JumpLabel(trueLabel));
        } else {
            String cond = br.getCond();
            String falseLabel = br.getIfFalse();
            int condWordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(cond)).getWordNumberToFp();
            mipsIns.add(new Lw("t0", "fp", 4 * condWordNumber));
            mipsIns.add(new Beqz("t0", falseLabel));
            mipsIns.add(new JumpLabel(trueLabel));
        }
    }

    private int getInt(GetInt getInt, MipsSymbolTable mipsSymbolTableNew, int funcWordSize) {
        int nowFuncWordSize = funcWordSize;
        String destName = getInt.getDest();
        mipsSymbolTableNew.addSymbol(destName, new MipsTempVar(nowFuncWordSize));
        int destWordNumber = nowFuncWordSize;
        nowFuncWordSize += 1;
        mipsIns.add(new SetValue("v0", 5));
        mipsIns.add(new Syscall());
        mipsIns.add(new Sw("v0", "fp", 4 * destWordNumber));
        return nowFuncWordSize;
    }

    private void funcReturn(FuncRet funcRet, MipsSymbolTable mipsSymbolTableNew, boolean isMain) {
        WordTypeCode funcType = funcRet.getFuncType();
        if (funcType == WordTypeCode.INTTK) {
            boolean isValue = funcRet.isValue();
            String index = funcRet.getRetIndex();
            if (isValue) {
                mipsIns.add(new SetValue("v0", Integer.parseInt(index)));
            } else {
                int returnValueWordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(index)).getWordNumberToFp();
                mipsIns.add(new Lw("v0", "fp", 4 * returnValueWordNumber));
            }
        }
        if (!isMain) {
            freeRa();
            mipsIns.add(new Jr());
        } else {
            mipsIns.add(new SetValue("v0", 10));
            mipsIns.add(new Syscall());
        }
    }

    private int compare(Compare compare, MipsSymbolTable mipsSymbolTableNew, int funcWordSize) {
        int nowFuncWordSize = funcWordSize;
        CmpType cmpType = compare.getCmpType();
        String op1 = compare.getOp1();
        String op2 = compare.getOp2();
        boolean op1IsValue = compare.isOp1Value();
        boolean op2IsValue = compare.isOp2Value();
        if (op1IsValue) {
            mipsIns.add(new SetValue("t1", Integer.parseInt(op1)));
        } else {
            mipsIns.add(new Lw("t1", "fp", 4 * ((MipsTempVar) mipsSymbolTableNew.getSymbol(op1)).getWordNumberToFp()));
        }
        if (op2IsValue) {
            mipsIns.add(new SetValue("t2", Integer.parseInt(op2)));
        } else {
            mipsIns.add(new Lw("t2", "fp", 4 * ((MipsTempVar) mipsSymbolTableNew.getSymbol(op2)).getWordNumberToFp()));
        }
        switch (cmpType) {
            case eq -> {
                mipsIns.add(new Subu("t0", "t1", "t2"));
                mipsIns.add(new Slt("t1", "t0", "0"));
                mipsIns.add(new Slt("t2", "0", "t0"));
                mipsIns.add(new Or("t0", "t1", "t2"));
                mipsIns.add(new SetValue("t1", 1));
                mipsIns.add(new Xor("t0", "t0", "t1"));
            }
            case ne -> {
                mipsIns.add(new Subu("t0", "t1", "t2"));
                mipsIns.add(new Slt("t1", "t0", "0"));
                mipsIns.add(new Slt("t2", "0", "t0"));
                mipsIns.add(new Or("t0", "t1", "t2"));
            }
            case sgt -> {
                mipsIns.add(new Subu("t1", "t2", "t1"));
                mipsIns.add(new Slt("t0", "t1", "0"));
            }
            case sge -> {
                mipsIns.add(new Subu("t1", "t1", "t2"));
                mipsIns.add(new Slt("t1", "t1", "0"));
                mipsIns.add(new SetValue("t2", 1));
                mipsIns.add(new Xor("t0", "t1", "t2"));
            }
            case slt -> {
                mipsIns.add(new Subu("t1", "t1", "t2"));
                mipsIns.add(new Slt("t0", "t1", "0"));
            }
            case sle -> {
                mipsIns.add(new Subu("t1", "t1", "t2"));
                mipsIns.add(new Slt("t1", "0", "t1"));
                mipsIns.add(new SetValue("t2", 1));
                mipsIns.add(new Xor("t0", "t1", "t2"));
            }
        }
        String resName = compare.getResIndex();
        mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize));
        int resWordNumber = nowFuncWordSize;
        nowFuncWordSize += 1;
        mipsIns.add(new Sw("t0", "fp", 4 * resWordNumber));
        return nowFuncWordSize;
    }

    private int getArrayEle(GetArrayElePtr getArrayElePtr, MipsSymbolTable mipsSymbolTableNew, int funcWordSize) {
        int nowFuncWordSize = funcWordSize;
        String arrName = getArrayElePtr.getArr();
        boolean isGlobal = getArrayElePtr.isGlobal();
        int dimen = getArrayElePtr.getDimen();
        int[] dimens = getArrayElePtr.getDimens();
        String[] index = getArrayElePtr.getIndex();
        boolean[] isValue = getArrayElePtr.getIsValue();
        String resName = getArrayElePtr.getResName();
        assert dimen == 1 || dimen == 2;
        if (isGlobal) {
            nowFuncWordSize = getEleIfGlobal(mipsSymbolTableNew, resName, nowFuncWordSize, arrName, dimen,
                    dimens, index, isValue);
        } else { //notGlobal
            nowFuncWordSize = getEleIfNotGlobal(mipsSymbolTableNew, resName, nowFuncWordSize, arrName, dimen,
                    dimens, index, isValue);
        }
        return nowFuncWordSize;
    }

    private int getEleIfNotGlobal(MipsSymbolTable mipsSymbolTableNew, String resName, int funcWordSize,
                                  String arrName, int dimen, int[] dimens, String[] index, boolean[] isValue) {
        int nowFuncWordSize = funcWordSize;
        MipsVar mipsVar = mipsSymbolTableNew.getSymbol(arrName);
        assert mipsVar instanceof MipsTempVar;
        int arrNumber = ((MipsTempVar) mipsVar).getWordNumberToFp();
        if (dimen == 1) {
            String sub = index[0];
            boolean subIsValue = isValue == null || isValue[0];
            if (subIsValue) {
                mipsIns.add(new Lw("t1", "fp", 4 * arrNumber));
                mipsIns.add(new SetValue("t2", 4 * Integer.parseInt(sub)));
                mipsIns.add(new Addu("t0", "t1", "t2"));
            } else {
                MipsVar subSymbol = mipsSymbolTableNew.getSymbol(sub);
                assert subSymbol instanceof MipsTempVar;
                int subWordNumber = ((MipsTempVar) subSymbol).getWordNumberToFp();
                mipsIns.add(new Lw("t2", "fp", 4 * subWordNumber));
                mipsIns.add(new SetValue("t1", 4));
                mipsIns.add(new Mult("t1", "t2"));
                mipsIns.add(new Mflo("t2"));
                mipsIns.add(new Lw("t1", "fp", 4 * arrNumber));
                mipsIns.add(new Addu("t0", "t1", "t2"));
            }
            mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize, 0, true, false));
            int resFuncWordSize = nowFuncWordSize;
            nowFuncWordSize += 1;
            mipsIns.add(new Sw("t0", "fp", 4 * resFuncWordSize));
            return nowFuncWordSize;
        } else {    //dimen == 2
            int indexSize = index.length;
            String sub1 = index[0];
            boolean sub1IsValue = isValue == null || isValue[0];
            if (indexSize == 1) { //arrAddr+4*i*dimens[1]
                if (sub1IsValue) {
                    mipsIns.add(new Lw("t1", "fp", 4 * arrNumber));
                    mipsIns.add(new SetValue("t2", 4 * Integer.parseInt(sub1) * dimens[1]));
                    mipsIns.add(new Addu("t0", "t1", "t2"));
                } else {
                    MipsVar subSymbol = mipsSymbolTableNew.getSymbol(sub1);
                    assert subSymbol instanceof MipsTempVar;
                    int subWordNumber = ((MipsTempVar) subSymbol).getWordNumberToFp();
                    mipsIns.add(new Lw("t2", "fp", 4 * subWordNumber));//将i加载出来
                    mipsIns.add(new SetValue("t1", 4 * dimens[1]));//加载4
                    mipsIns.add(new Mult("t1", "t2"));//4*i*dimens[1]
                    mipsIns.add(new Mflo("t2"));//4*i*...加载出来
                    mipsIns.add(new Lw("t1", "fp", 4 * arrNumber));
                    mipsIns.add(new Addu("t0", "t1", "t2"));
                }
                mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize, 1, true, false));
                int resFuncWordSize = nowFuncWordSize;
                nowFuncWordSize += 1;
                mipsIns.add(new Sw("t0", "fp", 4 * resFuncWordSize));
                return nowFuncWordSize;
            } else { // indexsize==2
                // arrAddr + 4 * (i*dimens[1] + j)
                String sub2 = index[1];
                boolean sub2IsValue = isValue == null || isValue[1];
                if (sub1IsValue && sub2IsValue) {
                    mipsIns.add(new Lw("t1", "fp", 4 * arrNumber));//t1:数组地址
                    mipsIns.add(new SetValue("t2",
                            4 * (Integer.parseInt(sub1) * dimens[1] + Integer.parseInt(sub2))));
                    mipsIns.add(new Addu("t0", "t1", "t2"));
                } else if (sub1IsValue) {
                    MipsVar sub2Symbol = mipsSymbolTableNew.getSymbol(sub2);
                    int sub2WordNumber = ((MipsTempVar) sub2Symbol).getWordNumberToFp();
                    mipsIns.add(new Lw("t2", "fp", 4 * sub2WordNumber));//把j加载出来
                    mipsIns.add(new SetValue("t1", 4));//4*j中的4
                    mipsIns.add(new Mult("t1", "t2"));//4*j
                    mipsIns.add(new Mflo("t2"));//4*j加载出来
                    mipsIns.add(new Lw("t1", "fp", 4 * arrNumber));
                    mipsIns.add(new Addu("t1", "t1", "t2"));
                    mipsIns.add(new SetValue("t2", 4 * Integer.parseInt(sub1) * dimens[1]));
                    mipsIns.add(new Addu("t0", "t1", "t2"));
                    //arrAddr + 4 * (i*dimens[1]) + 4 * j位置的值加载出来
                } else if (sub2IsValue) {
                    MipsVar sub1Symbol = mipsSymbolTableNew.getSymbol(sub1);
                    int sub1WordNumber = ((MipsTempVar) sub1Symbol).getWordNumberToFp();
                    mipsIns.add(new Lw("t2", "fp", 4 * sub1WordNumber));//把i加载出来
                    mipsIns.add(new SetValue("t1", 4 * dimens[1]));//4*dimens[1]
                    mipsIns.add(new Mult("t1", "t2"));//4*i*dimens[1]
                    mipsIns.add(new Mflo("t2"));//4*i*dimens[1]加载出来
                    mipsIns.add(new Lw("t1", "fp", 4 * arrNumber));
                    mipsIns.add(new Addu("t1", "t1", "t2"));
                    mipsIns.add(new SetValue("t2", 4 * Integer.parseInt(sub2)));
                    mipsIns.add(new Addu("t0", "t1", "t2"));
                } else {//sub1isLabel&sub2isLabel
                    MipsVar sub1Symbol = mipsSymbolTableNew.getSymbol(sub1);
                    int sub1WordNumber = ((MipsTempVar) sub1Symbol).getWordNumberToFp();
                    MipsVar sub2Symbol = mipsSymbolTableNew.getSymbol(sub2);
                    int sub2WordNumber = ((MipsTempVar) sub2Symbol).getWordNumberToFp();
                    mipsIns.add(new Lw("t2", "fp", 4 * sub1WordNumber));//把i加载出来
                    mipsIns.add(new SetValue("t1", 4 * dimens[1]));//4*dimens[1]
                    mipsIns.add(new Mult("t1", "t2"));//4*i*dimens[1]
                    mipsIns.add(new Mflo("t0"));//4*i*dimens[1]加载出来
                    mipsIns.add(new Lw("t2", "fp", 4 * sub2WordNumber));//把j加载出来
                    mipsIns.add(new SetValue("t1", 4));//4*j中的4
                    mipsIns.add(new Mult("t1", "t2"));//4*j
                    mipsIns.add(new Mflo("t2"));//4*j加载出来
                    mipsIns.add(new Addu("t2", "t0", "t2"));//4*i*dimens[1]+4*j
                    mipsIns.add(new Lw("t1", "fp", 4 * arrNumber));//数组地址
                    mipsIns.add(new Addu("t0", "t1", "t2"));
                }
                mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize, 0, true, false));
                int resFuncWordSize = nowFuncWordSize;
                nowFuncWordSize += 1;
                mipsIns.add(new Sw("t0", "fp", 4 * resFuncWordSize));
                return nowFuncWordSize;
            }
        }
    }

    private int getEleIfGlobal(MipsSymbolTable mipsSymbolTableNew, String resName, int funcWordSize,
                               String arrName, int dimen, int[] dimens, String[] index,
                               boolean[] isValue) {
        int nowFuncWordSize = funcWordSize;
        MipsVar mipsVar = mipsSymbolTableNew.getSymbol(arrName);
        assert mipsVar instanceof MipsGlobalVar;
        int arrAddr = ((MipsGlobalVar) mipsVar).getAddress();
        if (dimen == 1) {
            String sub = index[0];
            boolean subIsValue = isValue == null || isValue[0];
            if (subIsValue) {
                mipsIns.add(new SetValue("t0", arrAddr + 4 * Integer.parseInt(sub)));
            } else {
                MipsVar subSymbol = mipsSymbolTableNew.getSymbol(sub);
                assert subSymbol instanceof MipsTempVar;
                int subWordNumber = ((MipsTempVar) subSymbol).getWordNumberToFp();
                mipsIns.add(new Lw("t2", "fp", 4 * subWordNumber));
                mipsIns.add(new SetValue("t1", 4));
                mipsIns.add(new Mult("t1", "t2"));
                mipsIns.add(new Mflo("t2"));
                mipsIns.add(new SetValue("t1", arrAddr));
                mipsIns.add(new Addu("t0", "t2", "t1"));
            }
            mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize, 0, true, false));
            int resFuncWordSize = nowFuncWordSize;
            nowFuncWordSize += 1;
            mipsIns.add(new Sw("t0", "fp", 4 * resFuncWordSize));
            return nowFuncWordSize;
        } else {    //dimen == 2
            int indexSize = index.length;
            String sub1 = index[0];
            boolean sub1IsValue = isValue == null || isValue[0];
            if (indexSize == 1) { //arrAddr+4*i*dimens[1]
                if (sub1IsValue) {
                    mipsIns.add(new SetValue("t0", arrAddr + 4 * Integer.parseInt(sub1) * dimens[1]));
                } else {
                    MipsVar subSymbol = mipsSymbolTableNew.getSymbol(sub1);
                    assert subSymbol instanceof MipsTempVar;
                    int subWordNumber = ((MipsTempVar) subSymbol).getWordNumberToFp();
                    mipsIns.add(new Lw("t2", "fp", 4 * subWordNumber));//将i加载出来
                    mipsIns.add(new SetValue("t1", 4 * dimens[1]));//加载4*dimens[1]
                    mipsIns.add(new Mult("t1", "t2"));//4*i*dimens[1]
                    mipsIns.add(new Mflo("t2"));//4*i*dimens[1]加载出来
                    mipsIns.add(new SetValue("t1", arrAddr));
                    mipsIns.add(new Addu("t0", "t2", "t1"));//数组
                }
                mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize, 1, true, false));
                int resFuncWordSize = nowFuncWordSize;
                nowFuncWordSize += 1;
                mipsIns.add(new Sw("t0", "fp", 4 * resFuncWordSize));
                return nowFuncWordSize;
            } else { // indexsize==2
                // arrAddr + 4 * (i*dimens[1] + j)
                String sub2 = index[1];
                boolean sub2IsValue = isValue == null || isValue[1];
                if (sub1IsValue && sub2IsValue) {
                    mipsIns.add(new SetValue("t0",
                            arrAddr + 4 * (Integer.parseInt(sub1) * dimens[1] + Integer.parseInt(sub2))));
                } else if (sub1IsValue) {
                    MipsVar sub2Symbol = mipsSymbolTableNew.getSymbol(sub2);
                    int sub2WordNumber = ((MipsTempVar) sub2Symbol).getWordNumberToFp();
                    mipsIns.add(new Lw("t2", "fp", 4 * sub2WordNumber));//把j加载出来
                    mipsIns.add(new SetValue("t1", 4));//4*j中的4
                    mipsIns.add(new Mult("t1", "t2"));//4*j
                    mipsIns.add(new Mflo("t2"));//4*j加载出来
                    mipsIns.add(new SetValue("t1",
                            arrAddr + 4 * Integer.parseInt(sub1) * dimens[1]));
                    mipsIns.add(new Addu("t0", "t2", "t1"));
                    //arrAddr + 4 * (i*dimens[1]) + 4 * j位置的值加载出来
                } else if (sub2IsValue) {
                    MipsVar sub1Symbol = mipsSymbolTableNew.getSymbol(sub1);
                    int sub1WordNumber = ((MipsTempVar) sub1Symbol).getWordNumberToFp();
                    mipsIns.add(new Lw("t2", "fp", 4 * sub1WordNumber));//把i加载出来
                    mipsIns.add(new SetValue("t1", 4 * dimens[1]));//4*dimens[1]
                    mipsIns.add(new Mult("t1", "t2"));//4*i*dimens[1]
                    mipsIns.add(new Mflo("t2"));//4*i*dimens[1]加载出来
                    mipsIns.add(new SetValue("t1", arrAddr + 4 * Integer.parseInt(sub2)));
                    mipsIns.add(new Addu("t0", "t2", "t1"));
                } else {//sub1isLabel&sub2isLabel
                    MipsVar sub1Symbol = mipsSymbolTableNew.getSymbol(sub1);
                    int sub1WordNumber = ((MipsTempVar) sub1Symbol).getWordNumberToFp();
                    MipsVar sub2Symbol = mipsSymbolTableNew.getSymbol(sub2);
                    int sub2WordNumber = ((MipsTempVar) sub2Symbol).getWordNumberToFp();
                    mipsIns.add(new Lw("t2", "fp", 4 * sub1WordNumber));//把i加载出来
                    mipsIns.add(new SetValue("t1", 4 * dimens[1]));//4*dimens[1]
                    mipsIns.add(new Mult("t1", "t2"));//4*i*dimens[1]
                    mipsIns.add(new Mflo("t0"));//4*i*dimens[1]加载出来
                    mipsIns.add(new Lw("t2", "fp", 4 * sub2WordNumber));//把j加载出来
                    mipsIns.add(new SetValue("t1", 4));//4*j中的4
                    mipsIns.add(new Mult("t1", "t2"));//4*j
                    mipsIns.add(new Mflo("t2"));//4*j加载出来
                    mipsIns.add(new Addu("t1", "t0", "t2"));//4*i*dimens[1]+4*j
                    mipsIns.add(new SetValue("t2", arrAddr));
                    mipsIns.add(new Addu("t0", "t2", "t1"));
                }
                mipsSymbolTableNew.addSymbol(resName, new MipsTempVar(nowFuncWordSize, 0, true, false));
                int resFuncWordSize = nowFuncWordSize;
                nowFuncWordSize += 1;
                mipsIns.add(new Sw("t0", "fp", 4 * resFuncWordSize));
                return nowFuncWordSize;
            }
        }
    }

    private void protectRa() {
        mipsIns.add(new Sw("ra", "sp", 0));
        mipsIns.add(new StackPushByte(1));
    }

    private void freeRa() {
        mipsIns.add(new StackPushByte(-1));
        mipsIns.add(new Lw("ra", "sp", 0));
    }

    private int calculate(Calculate calculate, MipsSymbolTable mipsSymbolTableNew, int funcWordSize) {
        int nowFuncWordSize = funcWordSize;
        CalcuType calcuType = calculate.getCalcuType();
        boolean firstIsValue = calculate.isOp1Value();
        boolean secondIsValue = calculate.isOp2Value();
        String result = calculate.getResult();
        mipsSymbolTableNew.addSymbol(result, new MipsTempVar(nowFuncWordSize));
        int resultWordNumber = nowFuncWordSize;
        nowFuncWordSize += 1;
        assert !firstIsValue || !secondIsValue;
        if (firstIsValue) {
            int op1 = Integer.parseInt(calculate.getOp1());
            String op2 = calculate.getOp2();
            int op2WordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(op2)).getWordNumberToFp();
            mipsIns.add(new SetValue("t1", op1));
            mipsIns.add(new Lw("t2", "fp", 4 * op2WordNumber));
        } else if (secondIsValue) {
            String op1 = calculate.getOp1();
            int op1WordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(op1)).getWordNumberToFp();
            int op2 = Integer.parseInt(calculate.getOp2());
            mipsIns.add(new Lw("t1", "fp", 4 * op1WordNumber));
            mipsIns.add(new SetValue("t2", op2));
        } else {
            String op1 = calculate.getOp1();
            int op1WordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(op1)).getWordNumberToFp();
            String op2 = calculate.getOp2();
            int op2WordNumber = ((MipsTempVar) mipsSymbolTableNew.getSymbol(op2)).getWordNumberToFp();
            mipsIns.add(new Lw("t1", "fp", 4 * op1WordNumber));
            mipsIns.add(new Lw("t2", "fp", 4 * op2WordNumber));
        }
        switch (calcuType) {
            case add -> mipsIns.add(new Addu("t0", "t1", "t2"));
            case sub -> mipsIns.add(new Subu("t0", "t1", "t2"));
            case mul -> {
                mipsIns.add(new Mult("t1", "t2"));
                mipsIns.add(new Mflo("t0"));
            }
            case sdiv -> {
                mipsIns.add(new Div("t1", "t2"));
                mipsIns.add(new Mflo("t0"));
            }
            case srem -> {
                mipsIns.add(new Div("t1", "t2"));
                mipsIns.add(new Mfhi("t0"));
            }
        }
        mipsIns.add(new Sw("t0", "fp", 4 * resultWordNumber));
        return nowFuncWordSize;
    }
}
