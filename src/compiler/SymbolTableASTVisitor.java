package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

import java.util.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private final List<Map<String, STentry>> symTable = new ArrayList<>();
    private int nestingLevel = -1;
    private int offset = -2;
    int stErrors = 0;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        debug(n);
        openScope();
        visitAll(n.declist);
        visit(n.exp);
        closeScope(offset);
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        debug(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        debug(n);
        declareFunctionSymbol(n);
        int savedOffset = offset;
        offset = -2;
        Map<String, STentry> funScope = openScope();
        declareParams(n, funScope);
        visitAll(n.declist);
        visit(n.exp);
        closeScope(savedOffset);
        return null;
    }


    @Override
    public Void visitNode(VarNode n) {
        debug(n);
        visit(n.exp);
        STentry entry = new STentry(nestingLevel, n.type, offset--);
        declareSymbol("Var", n.id, n.getLine(), entry);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        debug(n);
        linkSTEntry(n, "Fun");
        visitAll(n.arglist);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        debug(n);
        linkSTEntry(n, "Var or Par");
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        debug(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        debug(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        debug(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        debug(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        debug(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    private Map<String, STentry> openScope() {
        Map<String, STentry> scope = new HashMap<>();
        symTable.add(scope);
        nestingLevel++;
        return scope;
    }

    private void closeScope(int savedOffset) {
        symTable.remove(nestingLevel--);
        offset = savedOffset;
    }

    private Map<String, STentry> currentScope() {
        return symTable.get(nestingLevel);
    }

    private STentry stLookup(String id) {
        for (int j = nestingLevel; j >= 0; j--) {
            STentry entry = symTable.get(j).get(id);
            if (entry != null) return entry;
        }
        return null;
    }

    private void declareSymbol(String kind, String id, int line, STentry entry) {
        if (currentScope().put(id, entry) != null) {
            System.out.println(kind + " id " + id + " at line " + line + " already declared");
            stErrors++;
        }
    }

    private void declareFunctionSymbol(FunNode n) {
        List<TypeNode> parTypes = extractParamTypes(n.parlist);
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), offset--);
        declareSymbol("Fun", n.id, n.getLine(), entry);
    }

    private void declareParams(FunNode n, Map<String, STentry> funScope) {
        int parOffset = 1;
        for (ParNode par : n.parlist) {
            STentry entry = new STentry(nestingLevel, par.type, parOffset++);
            if (funScope.put(par.id, entry) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        }
    }

    private void linkSTEntry(STNode n, String kind) {
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println(kind + " id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
    }

    private void debug(Node n) {
        if (print) printNode(n);
    }

    private List<TypeNode> extractParamTypes(List<ParNode> params) {
        List<TypeNode> types = new ArrayList<>();
        for (ParNode par : params) types.add(par.type);
        return types;
    }

    private void visitAll(List<? extends Node> nodes) {
        for (Node node : nodes) visit(node);
    }
}
