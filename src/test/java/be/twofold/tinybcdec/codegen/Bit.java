package be.twofold.tinybcdec.codegen;

final class Bit {
    private final Target target;
    private final int shift;

    Bit(Target target, int shift) {
        this.target = target;
        this.shift = shift;
    }

    public Target target() {
        return target;
    }

    public int shift() {
        return shift;
    }

    @Override
    public String toString() {
        return "Bit(" +
            "target=" + target + ", " +
            "shift=" + shift +
            ")";
    }
}
