package compiler;

import com.sun.jdi.ClassType;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

import java.util.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private final List<Map<String, STentry>> symTable = new ArrayList<>();
    private final Map<String, Map<String, STentry>> classTable = new HashMap<>();
    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level
    private int fieldOffset = -1;
    private int methodOffset = 0;
    int stErrors = 0;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null) entry = symTable.get(j--).get(id);
        return entry;
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = new HashMap<>();
        symTable.add(hm);
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        symTable.removeFirst();
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry entry =
                new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset--);
        // inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        // creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);
        int prevNLDecOffset =
                decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println(
                        "Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        // rimuovere la hashmap corrente poiche' esco dallo scope
        symTable.remove(nestingLevel--);
        decOffset =
                prevNLDecOffset; // restores counter for offset of declarations at previous nesting
        // level
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
        // inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Var id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        if (print) printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(MinusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println(
                    "Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(NotNode n) {
        if (print) printNode(n, n.bool.toString());
        visit(n.bool);
        return null;
    }

    @Override
    public Void visitNode(OrNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(ClassNode n) throws VoidException {
        if (print) printNode(n);
        /*
         * Declare class symbol in global scope.
         */
        final var globalScope = symTable.getFirst();
        final ClassTypeNode classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
        final var classEntry = new STentry(0, classType, decOffset--);
        globalScope.put(n.id, classEntry);
        /*
         * Create virtual table and appending scope in symbol table and class table.
         */
        final Map<String, STentry> virtualTable = new HashMap<>();
        symTable.add(virtualTable);
        classTable.put(n.id, virtualTable);
        nestingLevel++;
        /*
         * Declare all fields in the virtual table.
         */
        for (final var field : n.fields) {
            visit(field);
        }
        /*
         * Declare all methods in the virtual table.
         */
        for (final var method : n.methods) {
            visit(method);
        }
        /*
         * Closing class scope.
         */
        symTable.removeLast();
        nestingLevel--;
        fieldOffset = -1;
        methodOffset = 0;
        return null;
    }

    @Override
    public Void visitNode(FieldNode n) throws VoidException {
        if (print) printNode(n);
        final var virtualTable = symTable.get(nestingLevel);
        final var fieldEntry = new STentry(1, n.getType(), fieldOffset--);
        virtualTable.put(n.id, fieldEntry);
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) throws VoidException {
        if (print) printNode(n);
        final var virtualTable = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry methodEntry =
                new STentry(1, new ArrowTypeNode(parTypes, n.retType), methodOffset++);
        virtualTable.put(n.id, methodEntry);
        final HashMap<String, STentry> methodScope = new HashMap<>();
        symTable.add(methodScope);
        nestingLevel++;
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        symTable.removeLast();
        nestingLevel--;
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) throws VoidException {
        if (print) printNode(n);
        final var virtualTable = classTable.get(n.objId);
        if (virtualTable == null) {
            System.out.println("Object id " + n.objId + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            final STentry methodEntry = virtualTable.get(n.methodId);
            if (methodEntry == null) {
                System.out.println("Method id " + n.methodId + " at line " + n.getLine() + " not declared");
                stErrors++;
            }
            n.entry = methodEntry;
            n.nl = nestingLevel;
        }

        return null;
    }

    @Override
    public Void visitNode(NewNode n) throws VoidException {
        if (print) printNode(n);
        final var globalScope = symTable.getFirst();
        final STentry classEntry = globalScope.get(n.id);
        if (classEntry == null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            final var virtualTable = classTable.get(n.id);
            if (virtualTable == null) {
                System.out.println("Class id " + n.id + " at line " + n.getLine() + " not in class table");
                stErrors++;
            }
            for (Node arg : n.arglist) {
                visit(arg);
            }
        }
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) throws VoidException {
        return super.visitNode(n);
    }

    @Override
    public Void visitNode(RefTypeNode n) throws VoidException {
        return super.visitNode(n);
    }

    @Override
    public Void visitNode(EmptyTypeNode n) throws VoidException {
        return super.visitNode(n);
    }
}
