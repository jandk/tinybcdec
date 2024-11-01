package be.twofold.tinybcdec;

import java.util.*;

final class BC7Decoder extends BPTCDecoder {

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
        super(BlockFormat.BC7);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        Bits bits = Bits.from(src, srcPos);

        int modeIndex = Integer.numberOfTrailingZeros(src[srcPos]);
        bits.get(modeIndex + 1); // Skip mode bits
        if (modeIndex >= MODES.size()) {
            fillInvalidBlock(dst, dstPos, stride);
            return;
        }

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
                colors[0 * 4 + c] = (colors[0 * 4 + c] << 1) | sBit1;
                colors[1 * 4 + c] = (colors[1 * 4 + c] << 1) | sBit1;
                colors[2 * 4 + c] = (colors[2 * 4 + c] << 1) | sBit2;
                colors[3 * 4 + c] = (colors[3 * 4 + c] << 1) | sBit2;
            }
        }

        // Unpack colors
        int colorBits = mode.cb + (mode.epb + mode.spb);
        int alphaBits = mode.ab + (mode.epb + mode.spb);
        for (int i = 0; i < numColors; i++) {
            if (colorBits < 8) {
                colors[i * 4 + 0] = unpack(colors[i * 4 + 0], colorBits);
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
        long indexBits1 = indexBits(bits, mode.ib1, mode.ns, partition);
        long indexBits2 = indexBits(bits, mode.ib2, mode.ns, partition);

        int partitionTable = partitionTable(mode.ns, partition);

        int[] weights1 = weights(mode.ib1);
        int[] weights2 = weights(mode.ib2);

        int mask1 = (1 << mode.ib1) - 1;
        int mask2 = (1 << mode.ib2) - 1;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int index1 = (int) (indexBits1 & mask1);
                int cWeight = weights1[index1];
                int aWeight = weights1[index1];
                indexBits1 >>>= mode.ib1;

                if (mode.ib2 != 0) {
                    int index2 = (int) (indexBits2 & mask2);
                    if (selection) {
                        cWeight = weights2[index2];
                    } else {
                        aWeight = weights2[index2];
                    }
                    indexBits2 >>>= mode.ib2;
                }

                int pIndex = partitionTable & 3;
                partitionTable >>>= 2;
                int r = interpolate(colors[pIndex * 8 + 0], colors[pIndex * 8 + 4], cWeight);
                int g = interpolate(colors[pIndex * 8 + 1], colors[pIndex * 8 + 5], cWeight);
                int b = interpolate(colors[pIndex * 8 + 2], colors[pIndex * 8 + 6], cWeight);
                int a = interpolate(colors[pIndex * 8 + 3], colors[pIndex * 8 + 7], aWeight);

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

                ByteArrays.setInt(dst, dstPos, rgba(r, g, b, a));
                dstPos += BYTES_PER_PIXEL;
            }
            dstPos += stride - BLOCK_WIDTH * BYTES_PER_PIXEL;
        }
    }

    private static void fillInvalidBlock(byte[] dst, int dstPos, int bytesPerLine) {
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                ByteArrays.setInt(dst, dstPos, 0);
                dstPos += BYTES_PER_PIXEL;
            }
            dstPos += bytesPerLine - BLOCK_WIDTH * BYTES_PER_PIXEL;
        }
    }

    private int unpack(int i, int n) {
        assert n >= 4 && n <= 8;
        return i << (8 - n) | i >> (2 * n - 8);
    }

    private static final class Mode {
        private final int ns;
        private final int pb;
        private final int rb;
        private final int isb;
        private final int cb;
        private final int ab;
        private final int epb;
        private final int spb;
        private final int ib1;
        private final int ib2;

        private Mode(int ns, int pb, int rb, int isb, int cb, int ab, int epb, int spb, int ib1, int ib2) {
            this.ns = ns;
            this.pb = pb;
            this.rb = rb;
            this.isb = isb;
            this.cb = cb;
            this.ab = ab;
            this.epb = epb;
            this.spb = spb;
            this.ib1 = ib1;
            this.ib2 = ib2;
        }
    }
}
