package be.twofold.tinybcdec.codegen;

import java.util.*;

final class ModeDefinition {
    private final int mode;
    private final List<Range> ranges;
    private final List<Integer> pBits;

    ModeDefinition(int mode, List<Range> ranges, List<Integer> pBits) {
        this.mode = mode;
        this.ranges = ranges;
        this.pBits = pBits;
    }

    public int mode() {
        return mode;
    }

    public List<Range> ranges() {
        return ranges;
    }

    public List<Integer> pBits() {
        return pBits;
    }
}
