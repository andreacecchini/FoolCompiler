package compiler;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;
import java.util.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    /*
     * Constants.
     */
    private static final int GLOBAL_LEVEL = 0;
    private static final int CLASS_LEVEL = GLOBAL_LEVEL + 1;
    private static final int METHOD_OFFSET_START = 0;
    private static final int FIELD_OFFSET_START = -1;
    private static final int DECLARATION_OFFSET_START = -2;
    /* Symbol table as List of Tables. */
    private final List<Map<String, STentry>> symTable = new ArrayList<>();
    /* Class table. */
    private final Map<String, Map<String, STentry>> classTable = new HashMap<>();
    /* Current nesting level. */
    private int nestingLevel = GLOBAL_LEVEL;
    /* Counter for offset of local declarations at current nesting level. */
    private int decOffset = DECLARATION_OFFSET_START;
    /* Counter for offset of methods in a class. */
    private int methodOffset = METHOD_OFFSET_START;
    /* Symbol table errors. */
    int stErrors = 0;

    SymbolTableASTVisitor() {}

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

    /*
     * Gets the STentry of `id` starting the search from the inner scope
     * going to the outer ones.
     */
    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null) {
            entry = symTable.get(j--).get(id);
        }
        return entry;
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        /*
         * Opens global scope.
         */
        final Map<String, STentry> globalScope = new HashMap<>();
        symTable.add(globalScope);
        /*
         * Visits each global declaration.
         */
        for (final Node dec : n.declist) {
            visit(dec);
        }
        visit(n.exp);
        /*
         * Quits global scope.
         */
        symTable.removeFirst();
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        if (print) printNode(n);
        /*
         * No need to open global scope.
         */
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        if (print) printNode(n);
        /*
         * Declares function symbol in current scope.
         */
        final Map<String, STentry> currentScope = symTable.get(nestingLevel);
        final List<TypeNode> parTypes = new ArrayList<>();
        for (final ParNode par : n.parlist) {
            parTypes.add(par.getType());
        }
        final var funType = new ArrowTypeNode(parTypes, n.retType);
        final STentry entry = new STentry(nestingLevel, funType, decOffset--);
        if (currentScope.put(n.id, entry) != null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        n.setType(funType);
        /*
         * Opens function scope.
         */
        nestingLevel++;
        Map<String, STentry> functionScope = new HashMap<>();
        symTable.add(functionScope);
        /*
         * Stores counter for offset of declarations at previous nesting level
         * and restores local declaration offset.
         */
        final int prevNLDecOffset = decOffset;
        decOffset = -2;
        /*
         * Declares parameters inside the function.
         */
        int parOffset = 1;
        for (final ParNode par : n.parlist) {
            final var parEntry = new STentry(nestingLevel, par.getType(), parOffset++);
            if (functionScope.put(par.id, parEntry) != null) {
                System.out.println(
                        "Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        }
        /*
         * Declares locals inside the function.
         */
        for (final Node dec : n.declist) {
            visit(dec);
        }
        visit(n.exp);
        /*
         * Quits function scope.
         */
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset;
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        /*
         * Puts var declaration in current scope.
         */
        final Map<String, STentry> currentScope = symTable.get(nestingLevel);
        final STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
        if (currentScope.put(n.id, entry) != null) {
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
        /*
         * Checks if a STentry for current function call exists.
         */
        final STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            /*
             * Links function use to its declaration.
             */
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (final Node arg : n.arglist) {
            visit(arg);
        }
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        if (print) printNode(n);
        /*
         * Checks if a STentry for current id exists.
         */
        final STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println(
                    "Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            /*
             * Links id use to its declaration.
             * */
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

    /*
     * OOP part.
     */

    @Override
    public Void visitNode(ClassNode n) throws VoidException {
        if (print) printNode(n);
        /*
         * Declares class symbol in global scope.
         */
        final var globalScope = symTable.get(GLOBAL_LEVEL);
        final ClassTypeNode classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
        final var classEntry = new STentry(GLOBAL_LEVEL, classType, decOffset--);
        if (n.superId != null) {
            // inheritance
            final var superEntry = globalScope.get(n.superId);
            if (superEntry == null) {
                System.out.println("Extending from undefined class " + n.superId);
                stErrors++;
            } else {
                if (superEntry.type instanceof ClassTypeNode baseType) {
                    // inherits class type from super
                    classType.allFields.addAll(baseType.allFields);
                    classType.allMethods.addAll(baseType.allMethods);
                    n.superEntry = superEntry;
                } else {
                    System.out.println("Extending from a non class " + n.superId);
                    stErrors++;
                }
            }
        }
        if (globalScope.put(n.id, classEntry) != null) {
            System.out.println(
                    "Class id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        /*
         * Creates virtual table and appends it inside symbol and class table.
         */
        final Map<String, STentry> virtualTable = new HashMap<>();
        if (n.superId != null) {
            // inheritance
            final var superVirtualTable = classTable.get(n.superId);
            virtualTable.putAll(superVirtualTable);
        }
        symTable.add(virtualTable);
        nestingLevel++;
        /*
         * No need to check if `id` is already present in class table
         * because if it were a sterror would be thrown when appending in global scope.
         */
        classTable.put(n.id, virtualTable);
        /*
         * Declares all fields in the virtual table.
         */
        int fieldOffset = classType.allFields.isEmpty() ? FIELD_OFFSET_START : -classType.allFields.size()-1;
        for (final var field : n.fields) {
            final var oldEntry = virtualTable.get(field.id);
            if (oldEntry != null) {
                // field overriding
                final var overriddenOffset = oldEntry.offset;
                final var overriddenEntry = new STentry(CLASS_LEVEL, field.getType(), overriddenOffset);
                final var overriddenPos = -overriddenOffset - 1;
                if (overriddenEntry.type instanceof ArrowTypeNode) {
                    System.out.println("Overriding method " + field.id + " with a field");
                    stErrors++;
                } else {
                    virtualTable.put(field.id, overriddenEntry);
                    classType.allFields.set(overriddenPos, field.getType());
                }
            } else {
                // new field
                final var pos = -fieldOffset - 1;
                final var fieldEntry = new STentry(CLASS_LEVEL, field.getType(), fieldOffset--);
                virtualTable.put(field.id, fieldEntry);
                /* Updates class type with new field. */
                classType.allFields.add(pos, field.getType());
            }
        }
        /*
         * Declares all methods in the virtual table.
         */
        methodOffset = classType.allMethods.isEmpty() ? METHOD_OFFSET_START : classType.allMethods.size();
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
        /* Method type as ArrowType. */
        final List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) {
            parTypes.add(par.getType());
        }
        final var methodType = new ArrowTypeNode(parTypes, n.retType);
        final var oldEntry = virtualTable.get(n.id);
        if (oldEntry != null) {
            // overriding
            if (!(oldEntry.type instanceof ArrowTypeNode)) {
                System.out.println("Overriding field " + n.id + " with a method");
                stErrors++;
            } else {
                final var oldOffset = oldEntry.offset;
                n.offset = oldOffset;
                final var overridingEntry = new STentry(CLASS_LEVEL, methodType, oldOffset);
                virtualTable.put(n.id, overridingEntry);
            }
        } else {
            // new method
            n.offset = methodOffset;
            final STentry methodEntry = new STentry(CLASS_LEVEL, methodType, methodOffset++);
            virtualTable.put(n.id, methodEntry);
        }
        n.setType(methodType);
        /*
         * Opens method scope.
         */
        final HashMap<String, STentry> methodScope = new HashMap<>();
        symTable.add(methodScope);
        nestingLevel++;
        /*
         * Declares method parameters.
         */
        int parOffset = 1;
        for (final ParNode par : n.parlist) {
            final STentry parEntry = new STentry(nestingLevel, par.getType(), parOffset++);
            if (methodScope.put(par.id, parEntry) != null) {
                System.out.println(
                        "Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        }
        /*
         * Declares local method declarations.
         */
        final int prevNLDecOffset = decOffset;
        decOffset = DECLARATION_OFFSET_START;
        for (final Node dec : n.declist) {
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
         * Checks if a STEntry for the object exists
         * and, if exists, if the type stored in the entry is an object reference.
         */
        final STentry entry = stLookup(n.id1);
        if (entry == null) {
            System.out.println("Object id" + n.id1 + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            if (!(entry.type instanceof RefTypeNode)) {
                System.out.println(
                        "Object id " + n.id1 + " at line " + n.getLine() + " not a ref type");
                stErrors++;
            } else {
                /*
                 * Gets the virtual table of the class.
                 */
                final String className = ((RefTypeNode) entry.type).id;
                final var virtualTable = classTable.get(className);
                if (virtualTable == null) {
                    System.out.println(
                            "Class id "
                                    + n.id1
                                    + " at line "
                                    + n.getLine()
                                    + " not declared in class table");
                    stErrors++;
                } else {
                    /*
                     * Checks if the method is declared inside the virtual table.
                     */
                    final STentry methodEntry = virtualTable.get(n.id2);
                    if (methodEntry == null) {
                        System.out.println(
                                "Method id " + n.id2 + " at line " + n.getLine() + " not declared");
                        stErrors++;
                    } else {
                        /*
                         * Links method use to its declaration.
                         */
                        n.entry = entry;
                        n.methodEntry = methodEntry;
                        n.nl = nestingLevel;
                    }
                }
            }
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
                System.out.println(
                        "Class id " + n.id + " at line " + n.getLine() + " not in class table");
                stErrors++;
            } else {
                /*
                 * Visit constructor arguments.
                 */
                for (final Node arg : n.arglist) {
                    visit(arg);
                }
                /*
                 * Links constructor to class declaration.
                 */
                n.entry = classEntry;
            }
        }
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) throws VoidException {
        if (print) printNode(n);
        return null;
    }

    @Override
    public Void visitNode(RefTypeNode n) throws VoidException {
        if (print) printNode(n);
        return null;
    }

    @Override
    public Void visitNode(EmptyTypeNode n) throws VoidException {
        if (print) printNode(n);
        return null;
    }
}
