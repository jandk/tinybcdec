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
        int mask = (1 << count) - 1;
        int bits = (int) (lo & mask);
        lo = (lo >>> count) | ((hi & mask) << (64 - count));
        hi >>>= count;
        return bits;
    }

    int get1() {
        return get(1);
    }
}
