package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;

public class TypeRels {
    public static final Map<String, String> SUPER_TYPE = new HashMap<>();

    // valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o
    // BoolTypeNode
    public static boolean isSubtype(TypeNode a, TypeNode b) {
        // Checking method.
        if  (a instanceof ArrowTypeNode aFun && b instanceof ArrowTypeNode bFun) {
            return isSubTypeFun(aFun, bFun);
        }
        if (a instanceof RefTypeNode aRef && b instanceof RefTypeNode bRef) {
            return isSubTypeRel(aRef, bRef);
        }
        return  (a.getClass().equals(b.getClass()))
                || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode))
                || ((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode));
    }

    private static boolean isSubTypeRel(RefTypeNode a, RefTypeNode b) {
        // TODO: a -> b , b -> c then a -> c, where "->" means "inherits from"
        return a.id.equals(b.id) || SUPER_TYPE.get(a.id).equals(b.id);
    }

    private static boolean isSubTypeFun(ArrowTypeNode a, ArrowTypeNode b) {
        // co-variance on return type / contro-variance on parameters
        boolean parameters = true;
        if (!(a.parlist.size() == b.parlist.size())) {
            return false;
        }
        for (int i = 0; i < a.parlist.size(); i++) {
            parameters = parameters && isSubtype(b.parlist.get(i), a.parlist.get(i));
        }
        return isSubtype(a.ret, b.ret) && parameters;
    }
}
