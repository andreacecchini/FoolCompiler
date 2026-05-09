package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

public class TypeRels {
    /**
     * Maps each class ID to its direct superclass ID.
     * Populated during semantic analysis when class declarations are processed.
     */
    public static final Map<String, String> SUPER_TYPE = new HashMap<>();

    public static boolean isSubtype(TypeNode a, TypeNode b) {
        // Checking methods (on overrides).
        if (a instanceof ArrowTypeNode aFun) {
            return b instanceof ArrowTypeNode bFun && isFunSubType(aFun, bFun);
        }
        // Checking references.
        if (a instanceof RefTypeNode aRef) {
            return b instanceof RefTypeNode bRef && isRelSubType(aRef, bRef);
        }
        // Checking primitives
        return (a.getClass().equals(b.getClass()))
                || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode))
                || ((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode));
    }

    private static boolean isRelSubType(RefTypeNode a, RefTypeNode b) {
        // Same class
        if (a.id.equals(b.id)) {
            return true;
        }
        // Checking a is subtype of b
        String current = SUPER_TYPE.get(a.id);
        final Set<String> visited = new HashSet<>();
        while (current != null && visited.add(current)) {
            if (current.equals(b.id)) {
                return true;
            }
            current = SUPER_TYPE.get(current);
        }
        return false;
    }

    private static boolean isFunSubType(ArrowTypeNode a, ArrowTypeNode b) {
        // Return type is covariant; parameters are contravariant.
        if (a.parlist.size() != b.parlist.size()) {
            return false;
        }

        boolean parametersContravariant = IntStream.range(0, a.parlist.size())
                .allMatch(i -> isSubtype(b.parlist.get(i), a.parlist.get(i)));
        boolean retTypeCovariant = isSubtype(a.ret, b.ret);
        return parametersContravariant && retTypeCovariant;
    }
}
