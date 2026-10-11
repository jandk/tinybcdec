package be.twofold.tinybcdec.codegen;

import java.util.*;

abstract class Stmt {
    private Stmt() {
    }

    static final class Assign extends Stmt {
        private final Expr target;
        private final Expr value;

        private Assign(Expr target, Expr value) {
            this.target = Objects.requireNonNull(target);
            this.value = Objects.requireNonNull(value);
        }

        Assign(Expr.Index index, Expr value) {
            this((Expr) index, value);
        }

        Assign(Expr.Var var, Expr value) {
            this((Expr) var, value);
        }

        @Override
        public String toString() {
            return target + " = " + value + ";";
        }
    }

    static final class Declare extends Stmt {
        private final Expr.Var var;
        private final Expr value;

        Declare(Expr.Var var, Expr value) {
            this.var = Objects.requireNonNull(var);
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public String toString() {
            return var.declaration() + " = " + value + ";";
        }
    }
}
