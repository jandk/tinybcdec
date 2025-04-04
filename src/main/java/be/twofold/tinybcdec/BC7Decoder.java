package be.twofold.tinybcdec;

import java.util.*;

final class BC7Decoder extends BPTCDecoder {
    private static final int BPP = 4;

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

    BC7Decoder() {
        super(BlockFormat.BC7, BPP);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        int modeIndex = Integer.numberOfTrailingZeros(src[srcPos]);
        if (modeIndex >= MODES.size()) {
            fillInvalidBlock(dst, dstPos, stride);
            return;
        }

        Bits bits = Bits.from(src, srcPos);
        bits.get(modeIndex + 1); // Skip mode bits
        Mode mode = MODES.get(modeIndex);
        int partition = mode.pb != 0 ? bits.get(mode.pb) : 0;
        int rotation = mode.rb ? bits.get(2) : 0;
        boolean selection = mode.isb && bits.get1() != 0;

        int[] colors = new int[6 * 4];

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

        // Let's try a new method
        int partitions = partitions(mode.ns, partition);
        long indexBits1 = indexBits(bits, mode.ib1, mode.ns, partition);
        long indexBits2 = indexBits(bits, mode.ib2, mode.ns, partition);
        byte[] weights1 = weights(mode.ib1);
        byte[] weights2 = weights(mode.ib2);
        int mask1 = (1 << mode.ib1) - 1;
        int mask2 = (1 << mode.ib2) - 1;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int weight1 = weights1[(int) (indexBits1 & mask1)];
                int cWeight = weight1;
                int aWeight = weight1;
                indexBits1 >>>= mode.ib1;

                if (mode.ib2 != 0) {
                    int weight2 = weights2[(int) (indexBits2 & mask2)];
                    if (selection) {
                        cWeight = weight2;
                    } else {
                        aWeight = weight2;
                    }
                    indexBits2 >>>= mode.ib2;
                }

                int pIndex = partitions & 3;
                int r = interpolate(colors[pIndex * 8/**/], colors[pIndex * 8 + 4], cWeight);
                int g = interpolate(colors[pIndex * 8 + 1], colors[pIndex * 8 + 5], cWeight);
                int b = interpolate(colors[pIndex * 8 + 2], colors[pIndex * 8 + 6], cWeight);
                int a = interpolate(colors[pIndex * 8 + 3], colors[pIndex * 8 + 7], aWeight);
                partitions >>>= 2;

                if (rotation != 0) {
                    int t = a;
                    switch (rotation) {
                        case 1:
                            a = r;
                            r = t;
                            break;
                        case 2:
                            a = g;
                            g = t;
                            break;
                        case 3:
                            a = b;
                            b = t;
                            break;
                    }
                }

                int rgba = r | g << 8 | b << 16 | a << 24;
                ByteArrays.setInt(dst, dstPos + x * BPP, rgba);
            }
            dstPos += stride;
        }
    }

    private static void fillInvalidBlock(byte[] dst, int dstPos, int stride) {
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            Arrays.fill(dst, dstPos, dstPos + BLOCK_WIDTH * BPP, (byte) 0);
            dstPos += stride;
        }
    }

    private int unpack(int i, int n) {
        return i << (8 - n) | i >>> (2 * n - 8);
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
