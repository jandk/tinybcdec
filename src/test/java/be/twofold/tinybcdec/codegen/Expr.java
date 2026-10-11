package be.twofold.tinybcdec.codegen;

import java.util.*;
import java.util.stream.*;

abstract class Expr {
    private Expr() {
    }

    abstract int precedence();

    String wrap(Expr expr, int min) {
        return expr.precedence() < min ? "(" + expr + ")" : expr.toString();
    }

    public enum Op {
        OR("|", 1),
        AND("&", 2),
        SHL("<<", 3),
        USHR(">>>", 3),
        MUL("*", 4),
        ;

        private final String operator;
        private final int precedence;

        Op(String operator, int precedence) {
            this.operator = operator;
            this.precedence = precedence;
        }

        int precedence() {
            return precedence;
        }

        @Override
        public String toString() {
            return operator;
        }
    }

    static final class Binary extends Expr {
        private final Op op;
        private final Expr lhs;
        private final Expr rhs;

        Binary(Op op, Expr lhs, Expr rhs) {
            this.op = Objects.requireNonNull(op);
            this.lhs = Objects.requireNonNull(lhs);
            this.rhs = Objects.requireNonNull(rhs);
        }

        static Expr list(Op op, List<Expr> exprs) {
            return exprs.stream()
                .reduce((lhs, rhs) -> new Binary(op, lhs, rhs))
                .orElseThrow();
        }

        @Override
        int precedence() {
            return op.precedence();
        }

        @Override
        public String toString() {
            return wrap(lhs, precedence()) + " " + op + " " + wrap(rhs, precedence() + 1);
        }
    }

    static final class Cast extends Expr {
        private final String type;
        private final Expr expr;

        Cast(String type, Expr expr) {
            this.type = Objects.requireNonNull(type);
            this.expr = Objects.requireNonNull(expr);
        }

        @Override
        int precedence() {
            return 5;
        }

        @Override
        public String toString() {
            return "(" + type + ") " + wrap(expr, precedence() + 1);
        }
    }

    static final class Call extends Expr {
        private final String name;
        private final List<Expr> args;

        Call(String name, Expr... args) {
            this.name = Objects.requireNonNull(name);
            this.args = List.of(args);
        }

        @Override
        int precedence() {
            return 6;
        }

        @Override
        public String toString() {
            return args.stream()
                .map(Expr::toString)
                .collect(Collectors.joining(", ", name + "(", ")"));
        }
    }

    static final class Const extends Expr {
        private final int value;

        public Const(int value) {
            this.value = value;
        }

        @Override
        int precedence() {
            return 6;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    static final class LongConst extends Expr {
        private final long value;

        LongConst(long value) {
            this.value = value;
        }

        @Override
        int precedence() {
            return 6;
        }

        @Override
        public String toString() {
            String hex = String.format("%016X", value);
            return "0x" + hex.substring(0, 4) + "_" + hex.substring(4, 8) + "_" + hex.substring(8, 12) + "_" + hex.substring(12) + "L";
        }
    }

    static final class Index extends Expr {
        private final Var var;
        private final Expr index;

        public Index(Var var, Expr index) {
            this.var = Objects.requireNonNull(var);
            this.index = index;
        }

        @Override
        int precedence() {
            return 6;
        }

        @Override
        public String toString() {
            return var + "[" + index + "]";
        }
    }

    static final class Var extends Expr {
        private final String type;
        private final String name;

        Var(String type, String name) {
            this.type = Objects.requireNonNull(type);
            this.name = Objects.requireNonNull(name);
        }

        public String declaration() {
            return type + " " + name;
        }

        @Override
        int precedence() {
            return 6;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
