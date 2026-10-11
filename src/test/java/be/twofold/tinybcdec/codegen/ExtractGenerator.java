package be.twofold.tinybcdec.codegen;

import java.util.*;
import java.util.stream.*;

final class ExtractGenerator {
    private static final Expr.Var ENDPOINTS = new Expr.Var("int[]", "endpoints");
    private static final Expr.Var LO = new Expr.Var("long", "lo");
    private static final Expr.Var HI = new Expr.Var("long", "hi");

    public static void main(String[] args) {
        generateLayout(new BC6HLayout());
        generateLayout(new BC7Layout());
    }

    private static void generateLayout(Layout layout) {
        List<ModeDefinition> definitions = layout.generate();
        for (ModeDefinition definition : definitions) {
            System.out.println(generateMethod(definition));
        }
    }

    private static String generateMethod(ModeDefinition modeDefinition) {
        List<Integer> pBits = modeDefinition.pBits();

        List<Stmt> stmts = new ArrayList<>();
        Map<Integer, Expr.Var> pVars = new HashMap<>();
        for (int pos : pBits) {
            if (!pVars.containsKey(pos)) {
                Expr.Var var = new Expr.Var("int", "p" + pVars.size());
                pVars.put(pos, var);
                stmts.add(new Stmt.Declare(var, extract128(pos, 1, 0).get(0)));
            }
        }

        Map<Integer, List<Range>> groups = modeDefinition.ranges().stream()
            .sorted()
            .collect(Collectors.groupingBy(r -> r.target().arrayIndex(), TreeMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<Range>> entry : groups.entrySet()) {
            Integer index = entry.getKey();

            List<Expr> terms = new ArrayList<>();
            for (Range range : entry.getValue()) {
                terms.addAll(term(range));
            }
            if (!pBits.isEmpty()) {
                terms.add(pVars.get(pBits.get(index / 4)));
            }

            Expr allTerms = terms.stream()
                .reduce((a, b) -> new Expr.Binary(Expr.Op.OR, a, b))
                .orElseThrow();

            stmts.add(new Stmt.Assign(new Expr.Index(ENDPOINTS, index), allTerms));
        }

        String methodDeclaration = "private void readEndpoints" + modeDefinition.mode() + "(long lo, long hi, int[] endpoints) {\n";
        return stmts.stream()
            .map(stmt -> "    " + stmt)
            .collect(Collectors.joining("\n", methodDeclaration, "\n}\n"));
    }

    private static List<Expr> term(Range f) {
        if (f.reversed()) {
            Expr expr = extract128(f.start(), f.count(), 0).stream()
                .reduce((lhs, rhs) -> new Expr.Binary(Expr.Op.OR, lhs, rhs))
                .orElseThrow();
            Expr.Call rhs = new Expr.Call("reverse" + f.count(), expr);
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
