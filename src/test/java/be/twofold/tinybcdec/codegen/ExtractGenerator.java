package be.twofold.tinybcdec.codegen;

import java.util.*;
import java.util.stream.*;

final class ExtractGenerator {
    private static final Expr.Var LO = new Expr.Var("long", "lo");
    private static final Expr.Var HI = new Expr.Var("long", "hi");
    private static final Expr.Var ENDPOINTS = new Expr.Var("int[]", "endpoints");
    private static final Expr.Var REVERSE_6 = new Expr.Var("byte[]", "REVERSE_6");
    private static final List<Expr.Var> PARAMS = List.of(LO, HI, ENDPOINTS);

    public static void main(String[] args) {
        //generateLayout(new BC6HLayout());
        generateLayout(new BC7Layout());
    }

    private static void generateLayout(Layout layout) {
        List<ModeDefinition> definitions = layout.generate();
        for (ModeDefinition definition : definitions) {
            System.out.println(generateMethod(definition, layout.unpack()));
        }
    }

    private static String generateMethod(ModeDefinition modeDefinition, boolean unpack) {
        List<Endpoint> endpoints = modeDefinition.endpoints().stream()
            .sorted(Comparator.comparingInt((Endpoint e) -> e.target().arrayIndex()))
            .collect(Collectors.toList());

        List<Stmt> stmts = new ArrayList<>();
        Map<Integer, Expr.Var> pVars = new HashMap<>();
        for (Endpoint endpoint : endpoints) {
            int pos = endpoint.pBit();
            if (pos >= 0 && !pVars.containsKey(pos)) {
                Expr.Var var = new Expr.Var("int", "p" + pVars.size());
                pVars.put(pos, var);
                stmts.add(new Stmt.Declare(var, Expr.Binary.list(Expr.Op.OR, extract128(pos, 1, 0))));
            }
        }

        for (Endpoint endpoint : endpoints) {
            List<Expr> terms = new ArrayList<>();
            for (Range range : endpoint.ranges()) {
                terms.addAll(term(range));
            }
            if (endpoint.pBit() >= 0) {
                terms.add(pVars.get(endpoint.pBit()));
            }

            Expr value = terms.isEmpty()
                ? new Expr.Const(255)
                : Expr.Binary.list(Expr.Op.OR, terms);
            if (unpack && endpoint.bits() < 8) {
                value = new Expr.Call("unpack" + endpoint.bits(), value);
            }

            stmts.add(new Stmt.Assign(
                new Expr.Index(ENDPOINTS, new Expr.Const(endpoint.target().arrayIndex())),
                value
            ));
        }

        String signature = PARAMS.stream()
            .map(Expr.Var::declaration)
            .collect(Collectors.joining(", ", "private void readEndpoints" + modeDefinition.mode() + "(", ") {\n"));

        return stmts.stream()
            .map(stmt -> "    " + stmt)
            .collect(Collectors.joining("\n", signature, "\n}\n"));
    }

    private static List<Expr> term(Range f) {
        if (f.reversed()) {
            Expr expr = Expr.Binary.list(Expr.Op.OR, extract128(f.start(), f.count(), 0));
            Expr rhs = new Expr.Index(REVERSE_6, expr);
            return List.of(shift(rhs, -f.shift()));
        }
        return extract128(f.start(), f.count(), f.shift());
    }

    private static List<Expr> extract128(int pos, int count, int shift) {
        if (pos >= 64) {
            return List.of(extract64(HI, pos - 64, count, shift));
        }
        if (pos + count <= 64) {
            return List.of(extract64(LO, pos, count, shift));
        }

        int loCount = 64 - pos;
        int hiCount = count - loCount;

        Expr lhs = extract64(LO, pos, loCount, shift);
        Expr rhs = extract64(HI, 0, hiCount, loCount + shift);
        return List.of(lhs, rhs);
    }

    private static Expr extract64(Expr.Var var, int pos, int count, int shift) {
        int mask = ((1 << count) - 1) << shift;
        Expr shifted = shift(var, pos - shift);
        if (shift == 0 && pos + count == 64) {
            return new Expr.Cast("int", shifted);
        }

        return new Expr.Binary(Expr.Op.AND, new Expr.Cast("int", shifted), new Expr.Const(mask));
    }

    private static Expr shift(Expr src, int amount) {
        if (amount > 0) {
            return new Expr.Binary(Expr.Op.USHR, src, new Expr.Const(amount));
        }
        if (amount < 0) {
            return new Expr.Binary(Expr.Op.SHL, src, new Expr.Const(-amount));
        }
        return src;
    }
}
