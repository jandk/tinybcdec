package be.twofold.tinybcdec;

import java.util.*;

final class BC7Decoder extends BPTCDecoder {
    private static final int BPP = 4;

    private static final List<Mode> MODES = List.of(
        new Mode(3, 4, 0, 0, 4, 0, 1, 0, 3, 0),
        new Mode(2, 6, 0, 0, 6, 0, 0, 1, 3, 0),
        new Mode(3, 6, 0, 0, 5, 0, 0, 0, 2, 0),
        new Mode(2, 6, 0, 0, 7, 0, 1, 0, 2, 0),
        new Mode(1, 0, 2, 1, 5, 6, 0, 0, 2, 3),
        new Mode(1, 0, 2, 0, 7, 8, 0, 0, 2, 2),
        new Mode(1, 0, 0, 0, 7, 7, 1, 0, 4, 0),
        new Mode(2, 6, 0, 0, 5, 5, 1, 0, 2, 0)
    );

    BC7Decoder() {
        super(BlockFormat.BC7, BPP);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        Bits bits = Bits.from(src, srcPos);

        int modeIndex = Integer.numberOfTrailingZeros(src[srcPos]);
        if (modeIndex >= MODES.size()) {
            fillInvalidBlock(dst, dstPos, stride);
            return;
        }

        bits.get(modeIndex + 1); // Skip mode bits
        Mode mode = MODES.get(modeIndex);
        int partition = bits.get(mode.pb);
        int rotation = bits.get(mode.rb);
        boolean selection = bits.get(mode.isb) != 0;

        // Great, switching from an int[][] to an int[], increased perf by 40%.
        // I'll take the small readability hit.
        int numColors = mode.ns * 2;
        int[] colors = new int[numColors * 4];

        // Read colors
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
        if (mode.epb != 0) {
            for (int i = 0; i < numColors; i++) {
                int pBit = bits.get1();
                for (int c = 0; c < 4; c++) {
                    colors[i * 4 + c] = (colors[i * 4 + c] << 1) | pBit;
                }
            }
        }

        // Read shared p-bits
        if (mode.spb != 0) {
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
        int colorBits = mode.cb + (mode.epb + mode.spb);
        int alphaBits = mode.ab + (mode.epb + mode.spb);
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
        private final byte rb;
        private final byte isb;
        private final byte cb;
        private final byte ab;
        private final byte epb;
        private final byte spb;
        private final byte ib1;
        private final byte ib2;

        private Mode(int ns, int pb, int rb, int isb, int cb, int ab, int epb, int spb, int ib1, int ib2) {
            this.ns = (byte) ns;
            this.pb = (byte) pb;
            this.rb = (byte) rb;
            this.isb = (byte) isb;
            this.cb = (byte) cb;
            this.ab = (byte) ab;
            this.epb = (byte) epb;
            this.spb = (byte) spb;
            this.ib1 = (byte) ib1;
            this.ib2 = (byte) ib2;
        }
    }
}
