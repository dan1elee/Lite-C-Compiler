import codeGen.CodeGen;
import codeGen.mipsIns.MipsIns;
import codeGen.mipsIns.label.MipsLabel;
import codeGen.mipsIns.seg.SegBegin;
import errorHandler.ErrorDescription;
import lexer.Lexer;
import lexer.WordDescription;
import parser.Parser;
import parser.component.CompUnit;
import semanticAnalyzer.LLVMIR.LLVMIRIns;
import semanticAnalyzer.SemanticAnalyzer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        String inputFile = "testfile.txt";
        String outputFile = "mips.txt";
        String errorFile = "error.txt";
        Lexer lexer = new Lexer();
        ArrayList<WordDescription> descriptions = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(inputFile), StandardCharsets.UTF_8)) {
            int line = 0;
            while (scanner.hasNextLine()) {
                line++;
                descriptions.addAll(lexer.sentenceLexer(line, scanner.nextLine()));
            }
            WordDescription des = lexer.lastLine(line);
            if (des != null) {
                descriptions.add(des);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayList<ErrorDescription> errorDescriptions1 = new ArrayList<>();
        ArrayList<ErrorDescription> errorDescriptions2 = new ArrayList<>();
        Parser parser = new Parser(descriptions, errorDescriptions1);
        CompUnit compUnit = parser.parse();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(compUnit, errorDescriptions2);
        ArrayList<LLVMIRIns> llvmirIns = semanticAnalyzer.semanticAnalyze();
        if (errorDescriptions1.size() == 0 && errorDescriptions2.size() == 0) {
            CodeGen codeGen = new CodeGen(llvmirIns);
            ArrayList<MipsIns> mipsIns = codeGen.genMips();
            try (PrintWriter pw = new PrintWriter(outputFile, StandardCharsets.UTF_8)) {
                for (MipsIns ins : mipsIns) {
                    int space;
                    if (ins instanceof SegBegin) {
                        space = 0;
                    } else if (ins instanceof MipsLabel) {
                        space = 4;
                    } else {
                        space = 8;
                    }
                    pw.println(" ".repeat(space) + ins);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            ArrayList<ErrorDescription> errorDescriptions = new ArrayList<>(errorDescriptions1);
            errorDescriptions.addAll(errorDescriptions2);
            Collections.sort(errorDescriptions);
            try (PrintWriter pw = new PrintWriter(errorFile, StandardCharsets.UTF_8)) {
                for (ErrorDescription errorDescription : errorDescriptions) {
                    pw.println(errorDescription);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
