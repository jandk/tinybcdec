package be.twofold.tinybcdec.codegen;

final class Target {
    private final Field field;
    private final int index;

    Target(Field field, int index) {
        this.field = field;
        this.index = index;
    }

    public Field field() {
        return field;
    }

    public int index() {
        return index;
    }

    public int arrayIndex() {
        return 4 * index + field.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Target)) {
            return false;
        }

        Target target = (Target) obj;
        return field == target.field
            && index == target.index;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + field.hashCode();
        result = 31 * result + Integer.hashCode(index);
        return result;
    }

    @Override
    public String toString() {
        return "Target(" +
            "field=" + field + ", " +
            "index=" + index +
            ")";
    }
}
