package be.twofold.tinybcdec.codegen;

import java.util.*;

final class Range implements Comparable<Range> {
    private static final Comparator<Range> COMPARATOR = Comparator
        .comparingInt((Range r) -> r.target.arrayIndex())
        .thenComparing(Comparator.comparingInt((Range r) -> r.shift).reversed());

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
    public int compareTo(Range o) {
        return COMPARATOR.compare(this, o);
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
