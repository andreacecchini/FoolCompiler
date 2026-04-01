error id: file://<WORKSPACE>/src/compiler/Test.java:compiler/Test#printUsageAndExit#
file://<WORKSPACE>/src/compiler/Test.java
empty definition using pc, found symbol in pc: compiler/Test#printUsageAndExit#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 7230
uri: file://<WORKSPACE>/src/compiler/Test.java
text:
```scala
package compiler;

import compiler.exc.*;
import compiler.lib.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Test {
    public static void main(String[] args) throws Exception {
        boolean debugMode = Boolean.getBoolean("fool.debug");
        String sourceBaseName = sourceName(args);
        String sourceFileName = "src/" + sourceBaseName + ".fool";
        // ================ BACK-END ================
        // 1) Lexing e parsing
        ParsingResult parsingStepResult = lexingAndParsing(sourceFileName);
        // 2) Generazione AST
        Node ast = astGeneration(parsingStepResult.parseTree);
        // 3) Enriching AST
        int symbolTableErrors = enrichingAst(ast);
        // 4) Type checking
        int typeErrors = typeChecking(ast);
        int frontEndErrors = parsingStepResult.lexicalErrors
                + parsingStepResult.syntaxErrors
                + symbolTableErrors
                + typeErrors;
        System.out.println("You had a total of " + frontEndErrors + " front-end errors.\n");
        exitIfErrors(frontEndErrors);
        // ================ BACK-END ================
        // 1) Code generation
        String asmFileName = codeGeneration(sourceFileName, ast);
        // 2) Assembling
        AssembleResult assemblyStepResult = assemble(asmFileName, debugMode);
        int backEndErrors = assemblyStepResult.lexicalErrors + assemblyStepResult.syntaxErrors;
        System.out.println(
                "You had: "
                        + assemblyStepResult.lexicalErrors
                        + " lexical errors and "
                        + assemblyStepResult.syntaxErrors
                        + " syntax errors.\n");
        exitIfErrors(backEndErrors);
        // ================ EXECUTION ================
        executeOnVM(asmFileName, assemblyStepResult, debugMode);
    }

    private static ParsingResult lexingAndParsing(String sourceFileName) throws IOException {
        CharStream chars = CharStreams.fromFileName(sourceFileName);
        FOOLLexer lexer = new FOOLLexer(chars);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FOOLParser parser = new FOOLParser(tokens);

        System.out.println("Generating ST via lexer and parser.");
        ParseTree parseTree = parser.prog();
        int lexicalErrors = lexer.lexicalErrors;
        int syntaxErrors = parser.getNumberOfSyntaxErrors();
        System.out.println(
                "You had "
                        + lexicalErrors
                        + " lexical errors and "
                        + syntaxErrors
                        + " syntax errors.\n");

        return new ParsingResult(parseTree, lexicalErrors, syntaxErrors);
    }

    private static Node astGeneration(ParseTree parseTree) {
        System.out.println("Generating AST.");
        ASTGenerationSTVisitor visitor = new ASTGenerationSTVisitor(); // use true to visualize the ST
        Node ast = visitor.visit(parseTree);
        System.out.println();
        return ast;
    }

    private static int enrichingAst(Node ast) {
        System.out.println("Enriching AST via symbol table.");
        SymbolTableASTVisitor symtableVisitor = new SymbolTableASTVisitor();
        symtableVisitor.visit(ast);
        System.out.println("You had " + symtableVisitor.stErrors + " symbol table errors.\n");
        printEAST(ast);
        return symtableVisitor.stErrors;
    }

    private static void printEAST(Node ast) {
        System.out.println("Visualizing Enriched AST.");
        new PrintEASTVisitor().visit(ast);
        System.out.println();
    }

    private static int typeChecking(Node ast) {
        System.out.println("Checking Types.");
        try {
            TypeCheckEASTVisitor typeCheckVisitor = new TypeCheckEASTVisitor();
            TypeNode mainType = typeCheckVisitor.visit(ast);
            System.out.print("Type of main program expression is: ");
            new PrintEASTVisitor().visit(mainType);
        } catch (IncomplException e) {
            System.out.println(
                    "Could not determine main program expression type due to errors detected before type checking.");
        } catch (TypeException e) {
            System.out.println("Type checking error in main program expression: " + e.text);
        }
        System.out.println("You had " + FOOLlib.typeErrors + " type checking errors.\n");
        return FOOLlib.typeErrors;
    }

    private static String codeGeneration(String sourceFileName, Node ast) throws IOException {
        System.out.println("Generating code.");
        String asmFileName = sourceFileName + ".asm";
        String code = new CodeGenerationASTVisitor().visit(ast);
        try (BufferedWriter out = new BufferedWriter(new FileWriter(asmFileName))) {
            out.write(code);
        }
        System.out.println();
        return asmFileName;
    }

    private static AssembleResult assemble(String asmFileName, boolean debugMode)
            throws IOException {
        System.out.println("Assembling generated code.");
        if (debugMode) {
            CharStream charsASM = CharStreams.fromFileName(asmFileName);
            visualsvm.SVMLexer lexerASM = new visualsvm.SVMLexer(charsASM);
            CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
            visualsvm.SVMParser parserASM = new visualsvm.SVMParser(tokensASM);
            parserASM.assembly();
            return new AssembleResult(
                    parserASM.code,
                    parserASM.sourceMap,
                    lexerASM.lexicalErrors,
                    parserASM.getNumberOfSyntaxErrors());
        }

        CharStream charsASM = CharStreams.fromFileName(asmFileName);
        svm.SVMLexer lexerASM = new svm.SVMLexer(charsASM);
        CommonTokenStream tokensASM = new CommonTokenStream(lexerASM);
        svm.SVMParser parserASM = new svm.SVMParser(tokensASM);
        parserASM.assembly();
        return new AssembleResult(
                parserASM.code, null, lexerASM.lexicalErrors, parserASM.getNumberOfSyntaxErrors());
    }

    private static void executeOnVM(
            String asmFileName, AssembleResult assemblyStepResult, boolean debugMode)
            throws IOException {
        if (debugMode) {
            List<String> asmLines = Files.readAllLines(Path.of(asmFileName));
            System.out.println("Running generated code via Debug Stack Virtual Machine.");
            visualsvm.ExecuteVM vm = new visualsvm.ExecuteVM(
                    assemblyStepResult.machineCode, assemblyStepResult.sourceMap, asmLines);
            vm.cpu();
            return;
        }

        System.out.println("Running generated code via Stack Virtual Machine.");
        svm.ExecuteVM vm = new svm.ExecuteVM(assemblyStepResult.machineCode);
        vm.cpu();
    }

    private static void exitIfErrors(int errors) {
        if (errors > 0) {
            System.exit(1);
        }
    }

    private static String sourceName(String[] args) {
        if (args.length < 1 || args[0] == null || args[0].isBlank()) {
            printUsag@@eAndExit("Missing required source file name.");
        }
        String sourceBaseName = args[0].trim();
        String sourceFileName = "src/" + sourceBaseName + ".fool";
        if (Files.notExists(Path.of(sourceFileName))) {
            System.out.println("Source file not found " + sourceFileName + ". Expected path: src/<nomeFile>.fool");
            System.exit(1);
        }

        return sourceBaseName;
    }

    private record ParsingResult(ParseTree parseTree, int lexicalErrors, int syntaxErrors) {
    }

    private record AssembleResult(
            int[] machineCode, int[] sourceMap, int lexicalErrors, int syntaxErrors) {
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: compiler/Test#printUsageAndExit#