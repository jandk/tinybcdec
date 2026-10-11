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
    private final int[] endpoints = new int[3 * 2 * 4];
    private final int[] table = new int[3 * 8]; // Interpolated pixels per (subset, index)

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

        int[] endpoints = readEndpoints(modeIndex, lo, hi);
        int endpointBits = mode.ns * 2 * (3 * mode.cb + mode.ab) + (mode.epb ? mode.ns * 2 : 0) + (mode.spb ? mode.ns : 0);
        int endpointBitsHalf = endpointBits >>> 1;
        bits.getLong(endpointBitsHalf);
        bits.getLong(endpointBits - endpointBitsHalf);

        int numColors = mode.ns * 2;

        // Unpack endpoints
        int extraBits = (mode.epb ? 1 : 0) + (mode.spb ? 1 : 0);
        int colorBits = mode.cb + extraBits;
        int alphaBits = mode.ab + extraBits;
        for (int i = 0; i < numColors; i++) {
            if (colorBits < 8) {
                endpoints[i * 4/**/] = unpack(endpoints[i * 4/**/], colorBits);
                endpoints[i * 4 + 1] = unpack(endpoints[i * 4 + 1], colorBits);
                endpoints[i * 4 + 2] = unpack(endpoints[i * 4 + 2], colorBits);
            }
            if (mode.ab != 0 && alphaBits < 8) {
                endpoints[i * 4 + 3] = unpack(endpoints[i * 4 + 3], alphaBits);
            }
        }

        // Opaque mode
        if (mode.ab == 0) {
            for (int i = 0; i < numColors; i++) {
                endpoints[i * 4 + 3] = 0xFF;
            }
        }

        int ib1 = mode.ib1;
        int ib2 = mode.ib2;
        int[] table = this.table;

        if (ib2 == 0) {
            // modes 0, 1, 2, 3, 6 and 7
            long indexBits = indexBits(bits, ib1, mode.ns, partition);

            buildPalette(endpoints, table, weights(ib1), mode.ns, ib1);
            writePixels(dst, dstPos, stride, table, indexBits, partitions(mode.ns, partition), ib1);
        } else {
            // modes 4 and 5
            long indexBits1 = indexBits(bits, ib1, mode.ns, partition);
            long indexBits2 = indexBits(bits, ib2, mode.ns, partition);

            long cIndexBits = selection ? indexBits2 : indexBits1;
            long aIndexBits = selection ? indexBits1 : indexBits2;

            int cb = selection ? ib2 : ib1;
            int ab = selection ? ib1 : ib2;

            buildRotatedPalette(endpoints, table, weights(cb), weights(ab), cb, ab, rotation);
            writeRotatedPixels(dst, dstPos, stride, table, cIndexBits, aIndexBits, cb, ab);
        }
    }

    private int[] readEndpoints(int modeIndex, long lo, long hi) {
        int[] endpoints = this.endpoints;
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

    private void readEndpoints0(long lo, long hi, int[] endpoints) {
        int p0 = (int) (hi >>> 13) & 1;
        endpoints[0] = (int) (lo >>> 4) & 30 | p0;
        endpoints[1] = (int) (lo >>> 28) & 30 | p0;
        endpoints[2] = (int) (lo >>> 52) & 30 | p0;

        int p1 = (int) (hi >>> 14) & 1;
        endpoints[4] = (int) (lo >>> 8) & 30 | p1;
        endpoints[5] = (int) (lo >>> 32) & 30 | p1;
        endpoints[6] = (int) (lo >>> 56) & 30 | p1;

        int p2 = (int) (hi >>> 15) & 1;
        endpoints[8] = (int) (lo >>> 12) & 30 | p2;
        endpoints[9] = (int) (lo >>> 36) & 30 | p2;
        endpoints[10] = (int) (lo >>> 60) & 14 | (int) (hi << 4) & 16 | p2;

        int p3 = (int) (hi >>> 16) & 1;
        endpoints[12] = (int) (lo >>> 16) & 30 | p3;
        endpoints[13] = (int) (lo >>> 40) & 30 | p3;
        endpoints[14] = (int) hi & 30 | p3;

        int p4 = (int) (hi >>> 17) & 1;
        endpoints[16] = (int) (lo >>> 20) & 30 | p4;
        endpoints[17] = (int) (lo >>> 44) & 30 | p4;
        endpoints[18] = (int) (hi >>> 4) & 30 | p4;

        int p5 = (int) (hi >>> 18) & 1;
        endpoints[20] = (int) (lo >>> 24) & 30 | p5;
        endpoints[21] = (int) (lo >>> 48) & 30 | p5;
        endpoints[22] = (int) (hi >>> 8) & 30 | p5;
    }

    private void readEndpoints1(long lo, long hi, int[] endpoints) {
        int p0 = (int) (hi >>> 16) & 1;
        endpoints[0] = (int) (lo >>> 7) & 126 | p0;
        endpoints[1] = (int) (lo >>> 31) & 126 | p0;
        endpoints[2] = (int) (lo >>> 55) & 126 | p0;
        endpoints[4] = (int) (lo >>> 13) & 126 | p0;
        endpoints[5] = (int) (lo >>> 37) & 126 | p0;
        endpoints[6] = (int) (lo >>> 61) & 6 | (int) (hi << 3) & 120 | p0;

        int p1 = (int) (hi >>> 17) & 1;
        endpoints[8] = (int) (lo >>> 19) & 126 | p1;
        endpoints[9] = (int) (lo >>> 43) & 126 | p1;
        endpoints[10] = (int) (hi >>> 3) & 126 | p1;
        endpoints[12] = (int) (lo >>> 25) & 126 | p1;
        endpoints[13] = (int) (lo >>> 49) & 126 | p1;
        endpoints[14] = (int) (hi >>> 9) & 126 | p1;
    }

    private void readEndpoints2(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 9) & 31;
        endpoints[1] = (int) (lo >>> 39) & 31;
        endpoints[2] = (int) (hi >>> 5) & 31;
        endpoints[4] = (int) (lo >>> 14) & 31;
        endpoints[5] = (int) (lo >>> 44) & 31;
        endpoints[6] = (int) (hi >>> 10) & 31;
        endpoints[8] = (int) (lo >>> 19) & 31;
        endpoints[9] = (int) (lo >>> 49) & 31;
        endpoints[10] = (int) (hi >>> 15) & 31;
        endpoints[12] = (int) (lo >>> 24) & 31;
        endpoints[13] = (int) (lo >>> 54) & 31;
        endpoints[14] = (int) (hi >>> 20) & 31;
        endpoints[16] = (int) (lo >>> 29) & 31;
        endpoints[17] = (int) (lo >>> 59);
        endpoints[18] = (int) (hi >>> 25) & 31;
        endpoints[20] = (int) (lo >>> 34) & 31;
        endpoints[21] = (int) hi & 31;
        endpoints[22] = (int) (hi >>> 30) & 31;
    }

    private void readEndpoints3(long lo, long hi, int[] endpoints) {
        int p0 = (int) (hi >>> 30) & 1;
        endpoints[0] = (int) (lo >>> 9) & 254 | p0;
        endpoints[1] = (int) (lo >>> 37) & 254 | p0;
        endpoints[2] = (int) (hi >>> 1) & 254 | p0;

        int p1 = (int) (hi >>> 31) & 1;
        endpoints[4] = (int) (lo >>> 16) & 254 | p1;
        endpoints[5] = (int) (lo >>> 44) & 254 | p1;
        endpoints[6] = (int) (hi >>> 8) & 254 | p1;

        int p2 = (int) (hi >>> 32) & 1;
        endpoints[8] = (int) (lo >>> 23) & 254 | p2;
        endpoints[9] = (int) (lo >>> 51) & 254 | p2;
        endpoints[10] = (int) (hi >>> 15) & 254 | p2;

        int p3 = (int) (hi >>> 33) & 1;
        endpoints[12] = (int) (lo >>> 30) & 254 | p3;
        endpoints[13] = (int) (lo >>> 58) & 62 | (int) (hi << 6) & 192 | p3;
        endpoints[14] = (int) (hi >>> 22) & 254 | p3;
    }

    private void readEndpoints4(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 8) & 31;
        endpoints[1] = (int) (lo >>> 18) & 31;
        endpoints[2] = (int) (lo >>> 28) & 31;
        endpoints[3] = (int) (lo >>> 38) & 63;
        endpoints[4] = (int) (lo >>> 13) & 31;
        endpoints[5] = (int) (lo >>> 23) & 31;
        endpoints[6] = (int) (lo >>> 33) & 31;
        endpoints[7] = (int) (lo >>> 44) & 63;
    }

    private void readEndpoints5(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 8) & 127;
        endpoints[1] = (int) (lo >>> 22) & 127;
        endpoints[2] = (int) (lo >>> 36) & 127;
        endpoints[3] = (int) (lo >>> 50) & 255;
        endpoints[4] = (int) (lo >>> 15) & 127;
        endpoints[5] = (int) (lo >>> 29) & 127;
        endpoints[6] = (int) (lo >>> 43) & 127;
        endpoints[7] = (int) (lo >>> 58) | (int) (hi << 6) & 192;
    }

    private void readEndpoints6(long lo, long hi, int[] endpoints) {
        int p0 = (int) (lo >>> 63);
        endpoints[0] = (int) (lo >>> 6) & 254 | p0;
        endpoints[1] = (int) (lo >>> 20) & 254 | p0;
        endpoints[2] = (int) (lo >>> 34) & 254 | p0;
        endpoints[3] = (int) (lo >>> 48) & 254 | p0;

        int p1 = (int) hi & 1;
        endpoints[4] = (int) (lo >>> 13) & 254 | p1;
        endpoints[5] = (int) (lo >>> 27) & 254 | p1;
        endpoints[6] = (int) (lo >>> 41) & 254 | p1;
        endpoints[7] = (int) (lo >>> 55) & 254 | p1;
    }

    private void readEndpoints7(long lo, long hi, int[] endpoints) {
        int p0 = (int) (hi >>> 30) & 1;
        endpoints[0] = (int) (lo >>> 13) & 62 | p0;
        endpoints[1] = (int) (lo >>> 33) & 62 | p0;
        endpoints[2] = (int) (lo >>> 53) & 62 | p0;
        endpoints[3] = (int) (hi >>> 9) & 62 | p0;

        int p1 = (int) (hi >>> 31) & 1;
        endpoints[4] = (int) (lo >>> 18) & 62 | p1;
        endpoints[5] = (int) (lo >>> 38) & 62 | p1;
        endpoints[6] = (int) (lo >>> 58) & 62 | p1;
        endpoints[7] = (int) (hi >>> 14) & 62 | p1;

        int p2 = (int) (hi >>> 32) & 1;
        endpoints[8] = (int) (lo >>> 23) & 62 | p2;
        endpoints[9] = (int) (lo >>> 43) & 62 | p2;
        endpoints[10] = (int) (hi << 1) & 62 | p2;
        endpoints[11] = (int) (hi >>> 19) & 62 | p2;

        int p3 = (int) (hi >>> 33) & 1;
        endpoints[12] = (int) (lo >>> 28) & 62 | p3;
        endpoints[13] = (int) (lo >>> 48) & 62 | p3;
        endpoints[14] = (int) (hi >>> 4) & 62 | p3;
        endpoints[15] = (int) (hi >>> 24) & 62 | p3;
    }

    private static void buildPalette(int[] endpoints, int[] palette, byte[] weights, int ns, int ib) {
        int count = 1 << ib;
        for (int s = 0; s < ns; s++) {
            int p = s * 8;
            int r0 = endpoints[p/**/], g0 = endpoints[p + 1], b0 = endpoints[p + 2], a0 = endpoints[p + 3];
            int r1 = endpoints[p + 4], g1 = endpoints[p + 5], b1 = endpoints[p + 6], a1 = endpoints[p + 7];
            int base = s << ib;
            for (int i = 0; i < count; i++) {
                int w = weights[i];
                int r = interpolate(r0, r1, w);
                int g = interpolate(g0, g1, w);
                int b = interpolate(b0, b1, w);
                int a = interpolate(a0, a1, w);
                palette[base + i] = b | g << 8 | r << 16 | a << 24;
            }
        }
    }

    private static void buildRotatedPalette(
        int[] endpoints, int[] palette,
        byte[] cWeights, byte[] aWeights, int cb, int ab, int rotation
    ) {
        int r0 = endpoints[0], g0 = endpoints[1], b0 = endpoints[2], a0 = endpoints[3];
        int r1 = endpoints[4], g1 = endpoints[5], b1 = endpoints[6], a1 = endpoints[7];

        int shift = (3 - rotation) << 3;
        int keep = ~(0xFF << shift);

        int cCount = 1 << cb;
        for (int i = 0; i < cCount; i++) {
            int w = cWeights[i];
            int bgr = interpolate(b0, b1, w) | interpolate(g0, g1, w) << 8 | interpolate(r0, r1, w) << 16;
            palette[i] = (bgr & keep) | (((bgr >>> shift) & 0xFF) << 24);
        }

        int aCount = 1 << ab;
        for (int j = 0; j < aCount; j++) {
            palette[ALPHA_OFFSET + j] = interpolate(a0, a1, aWeights[j]) << shift;
        }
    }

    private static void writePixels(
        ByteBuffer dst, int dstPos, int stride,
        int[] table, long indexBits, int partitions, int ib
    ) {
        int mask = (1 << ib) - 1;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int bgra = table[((partitions & 3) << ib) + (int) (indexBits & mask)];
                ByteIO.setInt(dst, dstPos + x * BPP, bgra);
                indexBits >>>= ib;
                partitions >>>= 2;
            }
            dstPos += stride;
        }
    }

    private static void writeRotatedPixels(
        ByteBuffer dst, int dstPos, int stride,
        int[] table, long cIndexBits, long aIndexBits, int cb, int ab
    ) {
        int cMask = (1 << cb) - 1;
        int aMask = (1 << ab) - 1;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int bgra = table[(int) (cIndexBits & cMask)] | table[ALPHA_OFFSET + (int) (aIndexBits & aMask)];
                ByteIO.setInt(dst, dstPos + x * BPP, bgra);
                cIndexBits >>>= cb;
                aIndexBits >>>= ab;
            }
            dstPos += stride;
        }
    }

    private static int unpack(int i, int n) {
        i = i << (8 - n);
        return i | i >>> n;
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
