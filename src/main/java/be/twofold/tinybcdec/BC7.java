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
    private final int[] colors = new int[3 * 2 * 4];
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

        bits.get(modeIndex + 1); // Skip mode bits
        Mode mode = MODES.get(modeIndex);
        int partition = mode.pb != 0 ? bits.get(mode.pb) : 0;
        int rotation = mode.rb ? bits.get(2) : 0;
        boolean selection = mode.isb && bits.get1() != 0;

        int[] colors = this.colors;

        // Read colors
        int numColors = mode.ns * 2;
        for (int c = 0; c < 3; c++) {
            for (int i = 0; i < numColors; i++) {
                colors[i * 4 + c] = bits.get(mode.cb);
            }
        }

        // Read alphas
        if (mode.ab != 0) {
            for (int i = 0; i < numColors; i++) {
                colors[i * 4 + 3] = bits.get(mode.ab);
            }
        }

        // Read endpoint p-bits
        if (mode.epb) {
            for (int i = 0; i < numColors; i++) {
                int pBit = bits.get1();
                for (int c = 0; c < 4; c++) {
                    colors[i * 4 + c] = (colors[i * 4 + c] << 1) | pBit;
                }
            }
        }

        // Read shared p-bits
        if (mode.spb) {
            int sBit1 = bits.get1();
            int sBit2 = bits.get1();
            for (int c = 0; c < 4; c++) {
                colors[/*    */c] = (colors[/*    */c] << 1) | sBit1;
                colors[/**/4 + c] = (colors[/**/4 + c] << 1) | sBit1;
                colors[2 * 4 + c] = (colors[2 * 4 + c] << 1) | sBit2;
                colors[3 * 4 + c] = (colors[3 * 4 + c] << 1) | sBit2;
            }
        }

        // Unpack colors
        int extraBits = (mode.epb ? 1 : 0) + (mode.spb ? 1 : 0);
        int colorBits = mode.cb + extraBits;
        int alphaBits = mode.ab + extraBits;
        for (int i = 0; i < numColors; i++) {
            if (colorBits < 8) {
                colors[i * 4/**/] = unpack(colors[i * 4/**/], colorBits);
                colors[i * 4 + 1] = unpack(colors[i * 4 + 1], colorBits);
                colors[i * 4 + 2] = unpack(colors[i * 4 + 2], colorBits);
            }
            if (mode.ab != 0 && alphaBits < 8) {
                colors[i * 4 + 3] = unpack(colors[i * 4 + 3], alphaBits);
            }
        }

        // Opaque mode
        if (mode.ab == 0) {
            for (int i = 0; i < numColors; i++) {
                colors[i * 4 + 3] = 0xFF;
            }
        }

        int ib1 = mode.ib1;
        int ib2 = mode.ib2;
        int[] table = this.table;

        if (ib2 == 0) {
            // modes 0, 1, 2, 3, 6 and 7
            long indexBits = indexBits(bits, ib1, mode.ns, partition);

            buildTable(colors, table, weights(ib1), mode.ns, ib1);
            writePixels(dst, dstPos, stride, table, indexBits, partitions(mode.ns, partition), ib1);
        } else {
            // modes 4 and 5
            long indexBits1 = indexBits(bits, ib1, mode.ns, partition);
            long indexBits2 = indexBits(bits, ib2, mode.ns, partition);

            long cIndexBits = selection ? indexBits2 : indexBits1;
            long aIndexBits = selection ? indexBits1 : indexBits2;

            int cb = selection ? ib2 : ib1;
            int ab = selection ? ib1 : ib2;

            buildRotatedTables(colors, table, weights(cb), weights(ab), cb, ab, rotation);
            writeRotatedPixels(dst, dstPos, stride, table, cIndexBits, aIndexBits, cb, ab);
        }
    }

    private static void buildTable(int[] colors, int[] table, byte[] weights, int ns, int ib) {
        int count = 1 << ib;
        for (int s = 0; s < ns; s++) {
            int p = s * 8;
            int r0 = colors[p/*  */], g0 = colors[p + 1], b0 = colors[p + 2], a0 = colors[p + 3];
            int r1 = colors[p + 4], g1 = colors[p + 5], b1 = colors[p + 6], a1 = colors[p + 7];
            int base = s << ib;
            for (int i = 0; i < count; i++) {
                int w = weights[i];
                int r = interpolate(r0, r1, w);
                int g = interpolate(g0, g1, w);
                int b = interpolate(b0, b1, w);
                int a = interpolate(a0, a1, w);
                table[base + i] = b | g << 8 | r << 16 | a << 24;
            }
        }
    }

    private static void buildRotatedTables(
        int[] colors, int[] table,
        byte[] cWeights, byte[] aWeights, int cb, int ab, int rotation
    ) {
        int r0 = colors[0], g0 = colors[1], b0 = colors[2], a0 = colors[3];
        int r1 = colors[4], g1 = colors[5], b1 = colors[6], a1 = colors[7];

        int shift = (3 - rotation) << 3;
        int keep = ~(0xFF << shift);

        int cCount = 1 << cb;
        for (int i = 0; i < cCount; i++) {
            int w = cWeights[i];
            int bgr = interpolate(b0, b1, w) | interpolate(g0, g1, w) << 8 | interpolate(r0, r1, w) << 16;
            table[i] = (bgr & keep) | (((bgr >>> shift) & 0xFF) << 24);
        }

        int aCount = 1 << ab;
        for (int j = 0; j < aCount; j++) {
            table[ALPHA_OFFSET + j] = interpolate(a0, a1, aWeights[j]) << shift;
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
