package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

import java.util.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private static final int GLOBAL_LEVEL = 0;
    private static final int CLASS_LEVEL = GLOBAL_LEVEL + 1;
    public static final int METHOD_OFFSET_START = 0;
    public static final int FIELD_OFFSET_START = -1;
    public static final int DECLARATION_OFFSET_START = -2;
    private final List<Map<String, STentry>> symTable = new ArrayList<>();
    private final Map<String, Map<String, STentry>> classTable = new HashMap<>();
    private int nestingLevel = 0; // current nesting level
    private int decOffset = DECLARATION_OFFSET_START; // counter for offset of local declarations at current nesting level
    private int methodOffset = METHOD_OFFSET_START;
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
        final var funType = new ArrowTypeNode(parTypes, n.retType);
        STentry entry =
                new STentry(nestingLevel, funType, decOffset--);
        // inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        n.setType(funType);
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
         * Declares class symbol in global scope.
         */
        final var globalScope = symTable.get(GLOBAL_LEVEL);
        final ClassTypeNode classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
        final var classEntry = new STentry(GLOBAL_LEVEL, classType, decOffset--);
        if (globalScope.put(n.id, classEntry) != null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " already defined");
            stErrors++;
        }
        /*
         * Creates virtual table and appending scope in symbol table and class table.
         */
        final Map<String, STentry> virtualTable = new HashMap<>();
        symTable.add(virtualTable);
        // Check has already been done in global scope.
        classTable.put(n.id, virtualTable);
        nestingLevel++;
        /*
         * Declares all fields in the virtual table.
         */
        int fieldOffset = FIELD_OFFSET_START;
        for (final var field : n.fields) {
            final var pos = -fieldOffset - 1;
            final var fieldEntry = new STentry(CLASS_LEVEL, field.getType(), fieldOffset--);
            if (virtualTable.put(field.id, fieldEntry) != null) {
                System.out.println("Field id " + field.id + " at line " + field.getLine() + " already declared");
                stErrors++;
            }
            /* Updates class type with new field. */
            classType.allFields.add(pos, field.getType());
        }
        /*
         * Declares all methods in the virtual table.
         */
        for (final var method : n.methods) {
            final var pos = methodOffset;
            visit(method);
            /* Updates class type with new method. */
            classType.allMethods.add(pos, method.getType());
        }
        /*
         * Closing class scope.
         */
        symTable.removeLast();
        nestingLevel--;
        methodOffset = METHOD_OFFSET_START;
        return null;
    }

    @Override
    public Void visitNode(MethodNode n) throws VoidException {
        if (print) printNode(n);
        /*
         * Gets the virtual table of the class
         * and declare method symbol there.
         */
        final var virtualTable = symTable.get(nestingLevel);
        /*
         * Gets method type as ArrowType.
         */
        final List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) {
            parTypes.add(par.getType());
        }
        final var methodType = new ArrowTypeNode(parTypes, n.retType);
        final STentry methodEntry = new STentry(
                CLASS_LEVEL,
                methodType,
                methodOffset++);
        if (virtualTable.put(n.id, methodEntry) != null) {
            System.out.println("Method id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        n.setType(methodType);
        /*
         * Opens method scope and declare pars. and decls. there.
         */
        final HashMap<String, STentry> methodScope = new HashMap<>();
        symTable.add(methodScope);
        nestingLevel++;
        /*
         * Declare method parameters.
         */
        int parOffset = 1;
        for (ParNode par : n.parlist) {
            final STentry parEntry = new STentry(nestingLevel, par.getType(), parOffset++);
            if (methodScope.put(par.id, parEntry) != null) {
                System.out.println(
                        "Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        }
        /*
         * Declare method declarations.
         */
        int prevNLDecOffset = decOffset;
        decOffset = DECLARATION_OFFSET_START;
        for (Node dec : n.declist) {
            visit(dec);
        }
        visit(n.exp);
        /*
         * Quit method scope.
         */
        symTable.removeLast();
        nestingLevel--;
        decOffset = prevNLDecOffset;
        return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) throws VoidException {
        if (print) printNode(n);
        /*
         * Checks if the class virtual table exists.
         */
        final STentry entry = stLookup(n.id1);
        if (!(entry.type instanceof RefTypeNode)) {
            System.out.println("Object id " + n.id1 + " at line " + n.getLine() + " not a ref type");
            stErrors++;
        }
        final String className = ((RefTypeNode) entry.type).id;
        final var virtualTable = classTable.get(className);
        if (virtualTable == null) {
            System.out.println("Class id " + n.id1 + " at line " + n.getLine() + " not declared in class table");
            stErrors++;
        } else {
            /*
             * Checks if the method is declared inside the virtual table.
             */
            final STentry methodEntry = virtualTable.get(n.id2);
            if (methodEntry == null) {
                System.out.println("Method id " + n.id2 + " at line " + n.getLine() + " not declared");
                stErrors++;
            }
            /*
             * Links method use to its declaration.
             */
            n.entry = entry;
            n.methodEntry = methodEntry;
            n.nl = nestingLevel;
        }

        return null;
    }

    @Override
    public Void visitNode(NewNode n) throws VoidException {
        if (print) printNode(n);
        /*
         * Checks if the class symbol is declared in global scope.
         */
        final var globalScope = symTable.get(GLOBAL_LEVEL);
        final STentry classEntry = globalScope.get(n.id);
        if (classEntry == null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            /*
             * Checks if the class virtual table exists.
             */
            final var virtualTable = classTable.get(n.id);
            if (virtualTable == null) {
                System.out.println("Class id " + n.id + " at line " + n.getLine() + " not in class table");
                stErrors++;
            }
            /*
             * Visit constructor arguments.
             */
            for (Node arg : n.arglist) {
                visit(arg);
            }
            n.entry = classEntry;
        }
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) throws VoidException {
        return null;
    }

    @Override
    public Void visitNode(RefTypeNode n) throws VoidException {
        return null;
    }

    @Override
    public Void visitNode(EmptyTypeNode n) throws VoidException {
        return null;
    }
}
