package compiler;

import static compiler.lib.FOOLlib.*;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.FOOLlib;
import compiler.lib.Node;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Visitor template for code generation exercises.
 *
 * <p>Each visit method is intentionally left unimplemented so you can rebuild code generation from
 * scratch and drive it with tests.
 */
public class CodeGenerationExerciseASTVisitor extends BaseASTVisitor<String, VoidException> {

    private static final int TRUE = 1;
    private static final int FALSE = 0;
    private static final int NULL = 0;
    private static final String SUB = "sub";
    private static final String ADD = "add";
    private static final String DIV = "div";
    private static final String MULT = "mult";
    private static final String B = "b ";
    private static final String BE = "beq ";
    private static final String BLEQ = "bleq ";
    private static final String PUSH = "push ";
    private static final String LFP = "lfp";
    private static final String LW = "lw";
    private static final String LTM = "ltm";
    private static final String STM = "stm";
    private static final String JS = "js";
    private static final String PRINT = "print";
    private static final String LRA = "lra";
    private static final String SFP = "sfp";
    private static final String POP = "pop";
    private static final String SRA = "sra";
    private static final String CFP = "cfp";
    private static final String HALT = "halt";

    CodeGenerationExerciseASTVisitor() {}

    CodeGenerationExerciseASTVisitor(boolean debug) {
        super(false, debug);
    }

    private String binaryOperator(Node v1, Node v2, String op) {
        return nlJoin(visit(v1), visit(v2), op);
    }

    private String branch(
            String conditionCode,
            Function<String, String> jumpToTrue,
            String onTrue,
            String onFalse) {
        final var lThen = freshLabel();
        final var lEnd = freshLabel();
        return nlJoin(
                conditionCode,
                jumpToTrue.apply(lThen),
                onFalse,
                jump(lEnd),
                enterLabel(lThen),
                onTrue,
                enterLabel(lEnd));
    }

    private String branchLessOrEqual(String conditionCode, String onTrue, String onFalse) {
        return branch(conditionCode, this::jumpIfLessOrGreater, onTrue, onFalse);
    }

    private String branchEqual(String conditionCode, String onTrue, String onFalse) {
        return branch(conditionCode, this::jumpIfEqual, onTrue, onFalse);
    }

    private String branchTrue(String conditionCode, String onTrue, String onFalse) {
        return branchEqual(nlJoin(conditionCode, push(TRUE)), onTrue, onFalse);
    }

    @Override
    public String visitNode(ProgLetInNode n) {
        return nlJoin(
                push(NULL), // NULL as RA to make offset handling uniform.
                loads(n.declist),
                visit(n.exp),
                HALT,
                getCode() // Function code at the end of file
                );
    }

    @Override
    public String visitNode(ProgNode n) {
        return nlJoin(visit(n.exp), HALT);
    }

    @Override
    public String visitNode(FunNode n) {
        final var fEntry = freshFunLabel();
        putCode(
                nlJoin(
                        enterLabel(fEntry),
                        // -- Preamble
                        CFP,
                        LRA,
                        // -- End Preamble
                        loads(n.declist),
                        visit(n.exp),
                        // -- Prologue
                        STM,
                        popN(n.declist),
                        SRA,
                        POP,
                        popN(n.parlist),
                        SFP,
                        LTM,
                        LRA,
                        JS
                        // -- End Prologue
                        ));
        return PUSH + fEntry;
    }

    @Override
    public String visitNode(VarNode n) {
        return visit(n.exp);
    }

    @Override
    public String visitNode(PrintNode n) {
        return nlJoin(visit(n.exp), PRINT);
    }

    @Override
    public String visitNode(IfNode n) {
        return branchTrue(visit(n.cond), visit(n.th), visit(n.el));
    }

    @Override
    public String visitNode(EqualNode n) {
        return branchEqual(nlJoin(visit(n.left), visit(n.right)), push(TRUE), push(FALSE));
    }

    @Override
    public String visitNode(GreaterEqualNode n) {
        return branchLessOrEqual(nlJoin(visit(n.left), visit(n.right)), push(FALSE), push(TRUE));
    }

    @Override
    public String visitNode(LessEqualNode n) {
        return branchLessOrEqual(nlJoin(visit(n.left), visit(n.right)), push(TRUE), push(FALSE));
    }

    @Override
    public String visitNode(TimesNode n) {
        return binaryOperator(n.left, n.right, MULT);
    }

    @Override
    public String visitNode(DivNode n) {
        return binaryOperator(n.left, n.right, DIV);
    }

    @Override
    public String visitNode(PlusNode n) {
        return binaryOperator(n.left, n.right, ADD);
    }

    @Override
    public String visitNode(MinusNode n) {
        return binaryOperator(n.left, n.right, SUB);
    }

    @Override
    public String visitNode(CallNode n) {
        return nlJoin(
                LFP, // load old frame pointer
                loads(n.arglist.reversed()),
                LFP,
                followStaticChain(n.nl - n.entry.nl),
                STM,
                LTM, // AL
                LTM,
                push(n.entry.offset),
                ADD,
                LW,
                JS);
    }

    @Override
    public String visitNode(IdNode n) {
        return nlJoin(LFP, followStaticChain(n.nl - n.entry.nl), push(n.entry.offset), ADD, LW);
    }

    @Override
    public String visitNode(BoolNode n) {
        return push(n.val ? TRUE : FALSE);
    }

    @Override
    public String visitNode(NotNode n) {
        return branchTrue(visit(n.bool), push(FALSE), push(TRUE));
    }

    @Override
    public String visitNode(OrNode n) {
        return branchTrue(visit(n.left), push(TRUE), visit(n.right));
    }

    @Override
    public String visitNode(AndNode n) {
        return branchTrue(visit(n.left), visit(n.right), push(FALSE));
    }

    @Override
    public String visitNode(IntNode n) {
        return push(n.val);
    }

    private String push(int valueToPush) {
        return PUSH + valueToPush;
    }

    private String jumpIfLessOrGreater(String lTrue) {
        return BLEQ + lTrue;
    }

    private String jump(String lEnd) {
        return B + lEnd;
    }

    private String enterLabel(String label) {
        return label + ":";
    }

    private String jumpIfEqual(String lTrue) {
        return BE + lTrue;
    }

    private String followStaticChain(int nestingDifference) {
        return IntStream.range(0, nestingDifference).mapToObj(_ -> LW).reduce("", FOOLlib::nlJoin);
    }

    private String popN(List<? extends Node> nodes) {
        return nodes.stream().map(_ -> POP).reduce("", FOOLlib::nlJoin);
    }

    private String loads(List<? extends Node> declarations) {
        return declarations.stream().map(this::visit).reduce("", FOOLlib::nlJoin);
    }
}
