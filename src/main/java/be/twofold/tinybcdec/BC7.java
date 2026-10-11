package be.twofold.tinybcdec;

import java.nio.*;
import java.util.*;

final class BC7 extends BPTC {
    static final int BPP = 4;

    private static final List<Mode> MODES = List.of(
        new Mode(3, 4, F, F, 4, 0, T, F, 3, 0),
        new Mode(2, 6, F, F, 6, 0, F, T, 3, 0),
        new Mode(3, 6, F, F, 5, 0, F, F, 2, 0),
        new Mode(2, 6, F, F, 7, 0, T, F, 2, 0),
        new Mode(1, 0, T, T, 5, 6, F, F, 2, 3),
        new Mode(1, 0, T, F, 7, 8, F, F, 2, 2),
        new Mode(1, 0, F, F, 7, 7, T, F, 4, 0),
        new Mode(2, 6, F, F, 5, 5, T, F, 2, 0)
    );

    private static final int ALPHA_OFFSET = 8;

    private final Bits bits = new Bits();
    private final long[] endpoints = new long[3 * 2];
    private final int[] palette = new int[3 * 8]; // Interpolated pixels per (subset, index)

    BC7() {
        super(BPP);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        int modeIndex = Integer.numberOfTrailingZeros(ByteIO.getByte(src, srcPos));
        if (modeIndex >= MODES.size()) {
            fillInvalidBlock(dst, dstPos, stride, BPP);
            return;
        }

        Bits bits = this.bits;
        bits.read(src, srcPos);
        long lo = bits.lo;
        long hi = bits.hi;

        bits.get(modeIndex + 1); // Skip mode bits
        Mode mode = MODES.get(modeIndex);
        int partition = mode.pb != 0 ? bits.get(mode.pb) : 0;
        int rotation = mode.rb ? bits.get(2) : 0;
        boolean selection = mode.isb && bits.get1() != 0;

        long[] endpoints = readEndpoints(modeIndex, lo, hi);
        int endpointBits = mode.ns * 2 * (3 * mode.cb + mode.ab) + (mode.epb ? mode.ns * 2 : 0) + (mode.spb ? mode.ns : 0);
        int endpointBitsHalf = endpointBits >>> 1;
        bits.getLong(endpointBitsHalf);
        bits.getLong(endpointBits - endpointBitsHalf);

        int ib1 = mode.ib1;
        int ib2 = mode.ib2;
        int[] palette = this.palette;

        if (ib2 == 0) {
            // modes 0, 1, 2, 3, 6 and 7
            long indexBits = indexBits(bits, ib1, mode.ns, partition);

            buildPalette(endpoints, palette, weights(ib1), mode.ns, ib1);
            writePixels(dst, dstPos, stride, palette, indexBits, partitions(mode.ns, partition));
        } else {
            // modes 4 and 5
            long indexBits1 = indexBits(bits, ib1, mode.ns, partition);
            long indexBits2 = indexBits(bits, ib2, mode.ns, partition);

            long cIndexBits = selection ? indexBits2 : indexBits1;
            long aIndexBits = selection ? indexBits1 : indexBits2;

            int cb = selection ? ib2 : ib1;
            int ab = selection ? ib1 : ib2;

            buildRotatedPalette(endpoints, palette, weights(cb), weights(ab), cb, ab, rotation);
            writeRotatedPixels(dst, dstPos, stride, palette, cIndexBits, aIndexBits);
        }
    }

    private long[] readEndpoints(int modeIndex, long lo, long hi) {
        long[] endpoints = this.endpoints;
        switch (modeIndex) {
            case 0:
                readEndpoints0(lo, hi, endpoints);
                break;
            case 1:
                readEndpoints1(lo, hi, endpoints);
                break;
            case 2:
                readEndpoints2(lo, hi, endpoints);
                break;
            case 3:
                readEndpoints3(lo, hi, endpoints);
                break;
            case 4:
                readEndpoints4(lo, hi, endpoints);
                break;
            case 5:
                readEndpoints5(lo, hi, endpoints);
                break;
            case 6:
                readEndpoints6(lo, hi, endpoints);
                break;
            case 7:
                readEndpoints7(lo, hi, endpoints);
                break;
            default:
                throw new AssertionError();
        }
        return endpoints;
    }

    private void readEndpoints0(long lo, long hi, long[] endpoints) {
        endpoints[0] = 0x00FF_0000_0000_0000L | unpack5(lo << 28 & 0x001E_0000_0000L | lo >>> 12 & 0x001E_0000L | lo >>> 52 & 0x001EL | ((hi >>> 13) & 1) * 0x0001_0001_0001L);
        endpoints[1] = 0x00FF_0000_0000_0000L | unpack5(lo << 24 & 0x001E_0000_0000L | lo >>> 16 & 0x001E_0000L | lo >>> 56 & 0x001EL | ((hi >>> 14) & 1) * 0x0001_0001_0001L);
        endpoints[2] = 0x00FF_0000_0000_0000L | unpack5(lo << 20 & 0x001E_0000_0000L | lo >>> 20 & 0x001E_0000L | lo >>> 60 & 0x000EL | hi << 4 & 0x0010L | ((hi >>> 15) & 1) * 0x0001_0001_0001L);
        endpoints[3] = 0x00FF_0000_0000_0000L | unpack5(lo << 16 & 0x001E_0000_0000L | lo >>> 24 & 0x001E_0000L | hi & 0x001EL | ((hi >>> 16) & 1) * 0x0001_0001_0001L);
        endpoints[4] = 0x00FF_0000_0000_0000L | unpack5(lo << 12 & 0x001E_0000_0000L | lo >>> 28 & 0x001E_0000L | hi >>> 4 & 0x001EL | ((hi >>> 17) & 1) * 0x0001_0001_0001L);
        endpoints[5] = 0x00FF_0000_0000_0000L | unpack5(lo << 8 & 0x001E_0000_0000L | lo >>> 32 & 0x001E_0000L | hi >>> 8 & 0x001EL | ((hi >>> 18) & 1) * 0x0001_0001_0001L);
    }

    private void readEndpoints1(long lo, long hi, long[] endpoints) {
        long p0 = ((int) (hi >>> 16) & 1) * 0x0001_0001_0001L;
        long p1 = ((int) (hi >>> 17) & 1) * 0x0001_0001_0001L;
        endpoints[0] = 0x00FF_0000_0000_0000L | unpack7(lo << 25 & 0x007E_0000_0000L | lo >>> 15 & 0x007E_0000L | lo >>> 55 & 0x007EL | p0);
        endpoints[1] = 0x00FF_0000_0000_0000L | unpack7(lo << 19 & 0x007E_0000_0000L | lo >>> 21 & 0x007E_0000L | lo >>> 61 & 0x0006L | hi << 3 & 0x0078L | p0);
        endpoints[2] = 0x00FF_0000_0000_0000L | unpack7(lo << 13 & 0x007E_0000_0000L | lo >>> 27 & 0x007E_0000L | hi >>> 3 & 0x007EL | p1);
        endpoints[3] = 0x00FF_0000_0000_0000L | unpack7(lo << 7 & 0x007E_0000_0000L | lo >>> 33 & 0x007E_0000L | hi >>> 9 & 0x007EL | p1);
    }

    private void readEndpoints2(long lo, long hi, long[] endpoints) {
        endpoints[0] = 0x00FF_0000_0000_0000L | unpack5(lo << 23 & 0x001F_0000_0000L | lo >>> 23 & 0x001F_0000L | hi >>> 5 & 0x001FL);
        endpoints[1] = 0x00FF_0000_0000_0000L | unpack5(lo << 18 & 0x001F_0000_0000L | lo >>> 28 & 0x001F_0000L | hi >>> 10 & 0x001FL);
        endpoints[2] = 0x00FF_0000_0000_0000L | unpack5(lo << 13 & 0x001F_0000_0000L | lo >>> 33 & 0x001F_0000L | hi >>> 15 & 0x001FL);
        endpoints[3] = 0x00FF_0000_0000_0000L | unpack5(lo << 8 & 0x001F_0000_0000L | lo >>> 38 & 0x001F_0000L | hi >>> 20 & 0x001FL);
        endpoints[4] = 0x00FF_0000_0000_0000L | unpack5(lo << 3 & 0x001F_0000_0000L | lo >>> 43 & 0x001F_0000L | hi >>> 25 & 0x001FL);
        endpoints[5] = 0x00FF_0000_0000_0000L | unpack5(lo >>> 2 & 0x001F_0000_0000L | hi << 16 & 0x001F_0000L | hi >>> 30 & 0x001FL);
    }

    private void readEndpoints3(long lo, long hi, long[] endpoints) {
        endpoints[0] = 0x00FF_0000_0000_0000L | lo << 23 & 0x00FE_0000_0000L | lo >>> 21 & 0x00FE_0000L | hi >>> 1 & 0x00FEL | ((hi >>> 30) & 1) * 0x0001_0001_0001L;
        endpoints[1] = 0x00FF_0000_0000_0000L | lo << 16 & 0x00FE_0000_0000L | lo >>> 28 & 0x00FE_0000L | hi >>> 8 & 0x00FEL | ((hi >>> 31) & 1) * 0x0001_0001_0001L;
        endpoints[2] = 0x00FF_0000_0000_0000L | lo << 9 & 0x00FE_0000_0000L | lo >>> 35 & 0x00FE_0000L | hi >>> 15 & 0x00FEL | ((hi >>> 32) & 1) * 0x0001_0001_0001L;
        endpoints[3] = 0x00FF_0000_0000_0000L | lo << 2 & 0x00FE_0000_0000L | lo >>> 42 & 0x003E_0000L | hi << 22 & 0x00C0_0000L | hi >>> 22 & 0x00FEL | ((hi >>> 33) & 1) * 0x0001_0001_0001L;
    }

    private void readEndpoints4(long lo, long hi, long[] endpoints) {
        endpoints[0] = unpack6(lo << 10 & 0x003F_0000_0000_0000L) | unpack5(lo << 24 & 0x001F_0000_0000L | lo >>> 2 & 0x001F_0000L | lo >>> 28 & 0x001FL);
        endpoints[1] = unpack6(lo << 4 & 0x003F_0000_0000_0000L) | unpack5(lo << 19 & 0x001F_0000_0000L | lo >>> 7 & 0x001F_0000L | lo >>> 33 & 0x001FL);
    }

    private void readEndpoints5(long lo, long hi, long[] endpoints) {
        endpoints[0] = lo >>> 2 & 0x00FF_0000_0000_0000L | unpack7(lo << 24 & 0x007F_0000_0000L | lo >>> 6 & 0x007F_0000L | lo >>> 36 & 0x007FL);
        endpoints[1] = lo >>> 10 & 0x003F_0000_0000_0000L | hi << 54 & 0x00C0_0000_0000_0000L | unpack7(lo << 17 & 0x007F_0000_0000L | lo >>> 13 & 0x007F_0000L | lo >>> 43 & 0x007FL);
    }

    private void readEndpoints6(long lo, long hi, long[] endpoints) {
        endpoints[0] = lo & 0x00FE_0000_0000_0000L | lo << 26 & 0x00FE_0000_0000L | lo >>> 4 & 0x00FE_0000L | lo >>> 34 & 0x00FEL | (int) (lo >>> 63) * 0x0001_0001_0001_0001L;
        endpoints[1] = lo >>> 7 & 0x00FE_0000_0000_0000L | lo << 19 & 0x00FE_0000_0000L | lo >>> 11 & 0x00FE_0000L | lo >>> 41 & 0x00FEL | ((int) hi & 1) * 0x0001_0001_0001_0001L;
    }

    private void readEndpoints7(long lo, long hi, long[] endpoints) {
        endpoints[0] = unpack6(hi << 39 & 0x003E_0000_0000_0000L | lo << 19 & 0x003E_0000_0000L | lo >>> 17 & 0x003E_0000L | lo >>> 53 & 0x003EL | ((int) (hi >>> 30) & 1) * 0x0001_0001_0001_0001L);
        endpoints[1] = unpack6(hi << 34 & 0x003E_0000_0000_0000L | lo << 14 & 0x003E_0000_0000L | lo >>> 22 & 0x003E_0000L | lo >>> 58 & 0x003EL | ((int) (hi >>> 31) & 1) * 0x0001_0001_0001_0001L);
        endpoints[2] = unpack6(hi << 29 & 0x003E_0000_0000_0000L | lo << 9 & 0x003E_0000_0000L | lo >>> 27 & 0x003E_0000L | hi << 1 & 0x003EL | ((int) (hi >>> 32) & 1) * 0x0001_0001_0001_0001L);
        endpoints[3] = unpack6(hi << 24 & 0x003E_0000_0000_0000L | lo << 4 & 0x003E_0000_0000L | lo >>> 32 & 0x003E_0000L | hi >>> 4 & 0x003EL | ((int) (hi >>> 33) & 1) * 0x0001_0001_0001_0001L);
    }

    private long unpack5(long v) {
        return v << 3 | v >>> 2 & 0x0007_0007_0007_0007L;
    }

    private long unpack6(long v) {
        return v << 2 | v >>> 4 & 0x0003_0003_0003_0003L;
    }

    private long unpack7(long v) {
        return v << 1 | v >>> 6 & 0x0001_0001_0001_0001L;
    }

    private static void buildPalette(long[] endpoints, int[] palette, byte[] weights, int ns, int ib) {
        int epCount = ns * 2;
        int weightCount = 1 << ib;
        for (int p = 0; p < epCount; p += 2) {
            long e0 = endpoints[p];
            long e1 = endpoints[p + 1];
            for (int i = 0; i < weightCount; i++) {
                palette[p * 4 + i] = interpolate4(e0, e1, weights[i]);
            }
        }
    }

    private static void buildRotatedPalette(
        long[] endpoints, int[] palette,
        byte[] cWeights, byte[] aWeights, int cb, int ab, int rotation
    ) {
        long e0 = endpoints[0] & 0xFFFF_FFFF_FFFFL;
        long e1 = endpoints[1] & 0xFFFF_FFFF_FFFFL;
        int a0 = (int) (endpoints[0] >>> 48);
        int a1 = (int) (endpoints[1] >>> 48);

        int shift = (3 - rotation) << 3;
        int keep = ~(0xFF << shift);

        int cCount = 1 << cb;
        for (int i = 0; i < cCount; i++) {
            int bgr = interpolate4(e0, e1, cWeights[i]);
            palette[i] = (bgr & keep) | (((bgr >>> shift) & 0xFF) << 24);
        }

        int aCount = 1 << ab;
        for (int j = 0; j < aCount; j++) {
            palette[ALPHA_OFFSET + j] = interpolate(a0, a1, aWeights[j]) << shift;
        }
    }

    private static int interpolate4(long e0, long e1, int w) {
        long x = (e0 * (64 - w) + e1 * w + 0x0020_0020_0020_0020L) >>> 6 & 0x00FF_00FF_00FF_00FFL;
        x = (x | x >>> 8) & 0xFFFF_0000_FFFFL;
        return (int) (x | x >>> 16);
    }

    private static void writePixels(
        ByteBuffer dst, int dstPos, int stride,
        int[] palette, long indexBits, int partitions
    ) {
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int bgra = palette[((partitions & 3) << 3) + (int) (indexBits & 0xF)];
                ByteIO.setInt(dst, dstPos + x * BPP, bgra);
                indexBits >>>= 4;
                partitions >>>= 2;
            }
            dstPos += stride;
        }
    }

    private static void writeRotatedPixels(
        ByteBuffer dst, int dstPos, int stride,
        int[] palette, long cIndexBits, long aIndexBits
    ) {
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int bgra = palette[(int) (cIndexBits & 0xF)] | palette[ALPHA_OFFSET + (int) (aIndexBits & 0xF)];
                ByteIO.setInt(dst, dstPos + x * BPP, bgra);
                cIndexBits >>>= 4;
                aIndexBits >>>= 4;
            }
            dstPos += stride;
        }
    }

    private static final class Mode {
        private final byte ns;
        private final byte pb;
        private final boolean rb;
        private final boolean isb;
        private final byte cb;
        private final byte ab;
        private final boolean epb;
        private final boolean spb;
        private final byte ib1;
        private final byte ib2;

        private Mode(int ns, int pb, boolean rb, boolean isb, int cb, int ab, boolean epb, boolean spb, int ib1, int ib2) {
            this.ns = (byte) ns;
            this.pb = (byte) pb;
            this.rb = rb;
            this.isb = isb;
            this.cb = (byte) cb;
            this.ab = (byte) ab;
            this.epb = epb;
            this.spb = spb;
            this.ib1 = (byte) ib1;
            this.ib2 = (byte) ib2;
        }
    }
}
