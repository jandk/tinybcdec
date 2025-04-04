package be.twofold.tinybcdec;

final class Bits {
    private long lo;
    private long hi;

    private Bits(long lo, long hi) {
        this.lo = lo;
        this.hi = hi;
    }

    static Bits from(byte[] array, int index) {
        long lo = ByteArrays.getLong(array, index);
        long hi = ByteArrays.getLong(array, index + 8);
        return new Bits(lo, hi);
    }

    int get(int count) {
        return (int) getLong(count);
    }

    long getLong(int count) {
        long bits = lo & ((1L << count) - 1);
        lo = (lo >>> count) | (hi << (64 - count));
        hi = (hi >>> count);
        return bits;
    }

    int get1() {
        return get(1);
    }
}
