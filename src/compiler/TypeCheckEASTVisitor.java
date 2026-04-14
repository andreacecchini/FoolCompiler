package compiler;

import static compiler.TypeRels.*;

import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;


// visitNode(n) fa il type checking di un Node n e ritorna:
// - per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
// - per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
// (- per un tipo: "null"; controlla che il tipo non sia incompleto)
//
// visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode, TypeException> {

    TypeCheckEASTVisitor() {
        super(true);
    } // enables incomplete tree exceptions

    TypeCheckEASTVisitor(boolean debug) {
        super(true, debug);
    } // enables print for debugging

    // checks that a type object is visitable (not incomplete)
    private TypeNode ckvisit(TypeNode t) throws TypeException {
        visit(t);
        return t;
    }

    @Override
    public TypeNode visitNode(ProgLetInNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks global declaration.
         */
        for (final Node dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        /* Type checks `exp`. */
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(ProgNode n) throws TypeException {
        if (print) printNode(n);
        /* Type checks `exp`. */
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(FunNode n) throws TypeException {
        if (print) printNode(n, n.id);
        /*
         * Type checks local declarations.
         */
        for (final Node dec : n.declist) {
            try {
                visit(dec);
            } catch (IncomplException e) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        }
        /*
         * Type checks `exp`.
         */
        if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
            throw new TypeException("Wrong return type for function " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(VarNode n) throws TypeException {
        if (print) printNode(n, n.id);
        /*
         * Type checks `exp`.
         */
        if (!isSubtype(visit(n.exp), ckvisit(n.getType()))) {
            throw new TypeException("Incompatible value for variable " + n.id, n.getLine());
        }
        return null;
    }

    @Override
    public TypeNode visitNode(PrintNode n) throws TypeException {
        if (print) printNode(n);
        /* Type checks `exp`. */
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(IfNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `cond`.
         */
        if (!(isSubtype(visit(n.cond), new BoolTypeNode()))) {
            throw new TypeException("Non boolean condition in if", n.getLine());
        }
        /*
         * Type checks `then` and `else` branches.
         */
        final TypeNode t = visit(n.th);
        final TypeNode e = visit(n.el);
        if (isSubtype(t, e)) {
            return e;
        }
        if (isSubtype(e, t)) {
            return t;
        }
        throw new TypeException("Incompatible types in then-else branches", n.getLine());
    }

    @Override
    public TypeNode visitNode(EqualNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        final TypeNode l = visit(n.left);
        final TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l))) {
            throw new TypeException("Incompatible types in equal", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        final TypeNode l = visit(n.left);
        final TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l))) {
            throw new TypeException("Incompatible types in greater or equal", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(LessEqualNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        final TypeNode l = visit(n.left);
        final TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l))) {
            throw new TypeException("Incompatible types in less or equal", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(TimesNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in multiplication", n.getLine());
        }
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(DivNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in division", n.getLine());
        }
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(PlusNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in sum", n.getLine());
        }
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(MinusNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        if (!(isSubtype(visit(n.left), new IntTypeNode())
                && isSubtype(visit(n.right), new IntTypeNode()))) {
            throw new TypeException("Non integers in sub", n.getLine());
        }
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(CallNode n) throws TypeException {
        if (print) printNode(n, n.id);
        /*
         * Gets type from STentry.
         */
        final TypeNode t = visit(n.entry);
        /*
         * Checks if `id` is a function.
         */
        if (!(t instanceof ArrowTypeNode)) {
            throw new TypeException("Invocation of a non-function " + n.id, n.getLine());
        }
        final ArrowTypeNode at = (ArrowTypeNode) t;
        /*
         * Checks number of arguments.
         */
        if (!(n.arglist.size() == at.parlist.size())) {
            throw new TypeException(
                    "Wrong number of parameters in the invocation of " + n.id, n.getLine());
        }
        /*
         * Type checks arguments.
         */
        for (int i = 0; i < n.arglist.size(); i++)
            if (!(isSubtype(visit(n.arglist.get(i)), at.parlist.get(i))))
                throw new TypeException(
                        "Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.id,
                        n.getLine());
        return at.ret;
    }

    @Override
    public TypeNode visitNode(IdNode n) throws TypeException {
        if (print) printNode(n, n.id);
        /*
         * Gets type from STentry.
         */
        final TypeNode t = visit(n.entry);
        /*
         * Checks if `id` is a function id.
         */
        if (t instanceof ArrowTypeNode) {
            throw new TypeException("Wrong usage of function identifier " + n.id, n.getLine());
        }
        /*
         * Checks if `id` is a class id.
         */
        if (t instanceof ClassTypeNode) {
            throw new TypeException("Wrong usage of class identifier " + n.id, n.getLine());
        }
        return t;
    }

    @Override
    public TypeNode visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(NotNode n) throws TypeException {
        if (print) printNode(n, n.bool.toString());
        /*
         * Type checks not content.
         */
        if (!(isSubtype(visit(n.bool), new BoolTypeNode()))) {
            throw new TypeException("Non boolean argument in not", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(OrNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        if (!(isSubtype(visit(n.left), new BoolTypeNode())
                && isSubtype(visit(n.right), new BoolTypeNode()))) {
            throw new TypeException("Non booleans in or", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(AndNode n) throws TypeException {
        if (print) printNode(n);
        /*
         * Type checks `left` and `right`.
         */
        if (!(isSubtype(visit(n.left), new BoolTypeNode())
                && isSubtype(visit(n.right), new BoolTypeNode()))) {
            throw new TypeException("Non booleans in and", n.getLine());
        }
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return new IntTypeNode();
    }

    /*
     * OOP part.
     */

    @Override
    public TypeNode visitNode(ClassNode n) throws TypeException {
        if (print) printNode(n, n.id);
        /*
         * Setting super type rel.
         */
        if (n.superId != null) {
            // inheritance
            SUPER_TYPE.put(n.id, n.superId);
        }
        /*
         * Type checks class methods.
         */
        for (final Node method : n.methods) {
            visit(method);
        }
        if (n.superEntry != null) {
            if (n.superEntry.type instanceof ClassTypeNode baseType && n.type instanceof ClassTypeNode currentType) {
                for (int i = 0; i < baseType.allFields.size(); i++) {
                    if (!isSubtype(currentType.allFields.get(i), baseType.allFields.get(i))) {
                        throw new TypeException("Wrong type for field overriding", n.getLine());
                    }
                }
                for (int i = 0; i < baseType.allMethods.size(); i++) {
                    final TypeNode currentMethod = currentType.allMethods.get(i);
                    final var baseMethod = baseType.allMethods.get(i);
                    if (!isSubtype(currentMethod, baseMethod)) {
                        throw new TypeException("Wrong type for method overriding", currentMethod.getLine());
                    }
                }
            }
        }
        return null;
    }

    @Override
    public TypeNode visitNode(MethodNode n) throws TypeException {
        if (print) printNode(n, n.id);
        /*
         * Type checks local declarations.
         */
        for (final Node dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException _) {
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        /*
         * Type checks return type.
         */
        if (!isSubtype(visit(n.exp), ckvisit(n.retType))) {
            throw new TypeException("Wrong return type for method " + n.id, n.getLine());
        }
        return null;
    }

    @Override
    public TypeNode visitNode(ClassCallNode n) throws TypeException {
        if (print) printNode(n, n.id2);
        /*
         * Checks if `id2` type is an ArrowTypeNode.
         */
        TypeNode t = visit(n.methodEntry);
        if (!(t instanceof ArrowTypeNode)) {
            throw new TypeException("Invocation of a non-method " + n.id2, n.getLine());
        }
        ArrowTypeNode at = (ArrowTypeNode) t;
        /*
         * Checks number of arguments.
         */
        if (!(n.arglist.size() == at.parlist.size())) {
            throw new TypeException(
                    "Wrong number of parameters in the invocation of " + n.id2, n.getLine());
        }
        /*
         * Type checks arguments.
         */
        for (int i = 0; i < n.arglist.size(); i++) {
            if (!(isSubtype(visit(n.arglist.get(i)), at.parlist.get(i)))) {
                throw new TypeException(
                        "Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.id2,
                        n.getLine());
            }
        }
        return at.ret;
    }

    @Override
    public TypeNode visitNode(NewNode n) throws TypeException {
        if (print) printNode(n, n.id);
        ClassTypeNode ct = (ClassTypeNode) visit(n.entry);
        /*
         * Checks number of arguments.
         */
        if (n.arglist.size() != ct.allFields.size()) {
            throw new TypeException("Wrong number of arguments in class", n.getLine());
        }
        /*
         * Type checks constructor arguments.
         */
        for (int i = 0; i < n.arglist.size(); i++) {
            if (!(isSubtype(visit(n.arglist.get(i)), ct.allFields.get(i)))) {
                throw new TypeException(
                        "Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.id,
                        n.getLine());
            }
        }
        return new RefTypeNode(n.id);
    }

    @Override
    public TypeNode visitNode(EmptyNode n) throws TypeException {
        return new EmptyTypeNode();
    }

    // gestione tipi incompleti	(se lo sono lancia eccezione)

    @Override
    public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (Node par : n.parlist) visit(par);
        visit(n.ret, "->"); // marks return type
        return null;
    }

    @Override
    public TypeNode visitNode(BoolTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public TypeNode visitNode(IntTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public TypeNode visitNode(RefTypeNode n) throws TypeException {
        if (print) printNode(n);
        return null;
    }

    @Override
    public TypeNode visitNode(EmptyTypeNode n) throws TypeException {
        if (print) printNode(n);
        return null;
    }

    // STentry (ritorna campo type)

    @Override
    public TypeNode visitSTentry(STentry entry) throws TypeException {
        if (print) printSTentry("type");
        return ckvisit(entry.type);
    }
}
