package be.twofold.tinybcdec.codegen;

final class Range {
    private final Target target;
    private final int shift;
    private final int start;
    private final int count;
    private final boolean reversed;

    Range(Target target, int shift, int start, int count, boolean reversed) {
        this.target = target;
        this.shift = shift;
        this.start = start;
        this.count = count;
        this.reversed = reversed;
    }

    public Target target() {
        return target;
    }

    public int shift() {
        return shift;
    }

    public int start() {
        return start;
    }

    public int count() {
        return count;
    }

    public boolean reversed() {
        return reversed;
    }

    @Override
    public String toString() {
        return "Range(" +
            "target=" + target + ", " +
            "shift=" + shift + ", " +
            "start=" + start + ", " +
            "count=" + count + ", " +
            "reversed=" + reversed +
            ")";
    }
}
