package be.twofold.tinybcdec.codegen;

import java.util.*;

final class Endpoint {
    private final Target target;
    private final List<Range> ranges;
    private final int pBit;
    private final int bits;

    Endpoint(Target target, List<Range> ranges, int pBit, int bits) {
        this.target = Objects.requireNonNull(target);
        this.ranges = List.copyOf(ranges);
        this.pBit = pBit;
        this.bits = bits;
    }

    public Target target() {
        return target;
    }

    public List<Range> ranges() {
        return ranges;
    }

    public int pBit() {
        return pBit;
    }

    public int bits() {
        return bits;
    }
}
