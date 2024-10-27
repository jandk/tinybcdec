package be.twofold.tinybcdec;

import java.util.*;

final class BC7Decoder extends BlockDecoder {
    static final int[] SUBSET2 = {
        0x50505050, 0x40404040, 0x54545454, 0x54505040, 0x50404000, 0x55545450, 0x55545040, 0x54504000,
        0x50400000, 0x55555450, 0x55544000, 0x54400000, 0x55555440, 0x55550000, 0x55555500, 0x55000000,
        0x55150100, 0x00004054, 0x15010000, 0x00405054, 0x00004050, 0x15050100, 0x05010000, 0x40505054,
        0x00404050, 0x05010100, 0x14141414, 0x05141450, 0x01155440, 0x00555500, 0x15014054, 0x05414150,
        0x44444444, 0x55005500, 0x11441144, 0x05055050, 0x05500550, 0x11114444, 0x41144114, 0x44111144,
        0x15055054, 0x01055040, 0x05041050, 0x05455150, 0x14414114, 0x50050550, 0x41411414, 0x00141400,
        0x00041504, 0x00105410, 0x10541000, 0x04150400, 0x50410514, 0x41051450, 0x05415014, 0x14054150,
        0x41050514, 0x41505014, 0x40011554, 0x54150140, 0x50505500, 0x00555050, 0x15151010, 0x54540404,
    };

    private static final int[] SUBSET3 = {
        0xaa685050, 0x6a5a5040, 0x5a5a4200, 0x5450a0a8, 0xa5a50000, 0xa0a05050, 0x5555a0a0, 0x5a5a5050,
        0xaa550000, 0xaa555500, 0xaaaa5500, 0x90909090, 0x94949494, 0xa4a4a4a4, 0xa9a59450, 0x2a0a4250,
        0xa5945040, 0x0a425054, 0xa5a5a500, 0x55a0a0a0, 0xa8a85454, 0x6a6a4040, 0xa4a45000, 0x1a1a0500,
        0x0050a4a4, 0xaaa59090, 0x14696914, 0x69691400, 0xa08585a0, 0xaa821414, 0x50a4a450, 0x6a5a0200,
        0xa9a58000, 0x5090a0a8, 0xa8a09050, 0x24242424, 0x00aa5500, 0x24924924, 0x24499224, 0x50a50a50,
        0x500aa550, 0xaaaa4444, 0x66660000, 0xa5a0a5a0, 0x50a050a0, 0x69286928, 0x44aaaa44, 0x66666600,
        0xaa444444, 0x54a854a8, 0x95809580, 0x96969600, 0xa85454a8, 0x80959580, 0xaa141414, 0x96960000,
        0xaaaa1414, 0xa05050a0, 0xa0a5a5a0, 0x96000000, 0x40804080, 0xa9a8a9a8, 0xaaaaaa44, 0x2a4a5254,
    };

    static final int[] ANCHOR_11 = {
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, +2, +8, +2, +2, +8, +8, 15,
        +2, +8, +2, +2, +8, +8, +2, +2,
        15, 15, +6, +8, +2, +8, 15, 15,
        +2, +8, +2, +2, +2, 15, 15, +6,
        +6, +2, +6, +8, 15, 15, +2, +2,
        15, 15, 15, 15, 15, +2, +2, 15,
    };

    private static final int[] ANCHOR_21 = {
        +3, +3, 15, 15, +8, +3, 15, 15,
        +8, +8, +6, +6, +6, +5, +3, +3,
        +3, +3, +8, 15, +3, +3, +6, 10,
        +5, +8, +8, +6, +8, +5, 15, 15,
        +8, 15, +3, +5, +6, 10, +8, 15,
        15, +3, 15, +5, 15, 15, 15, 15,
        +3, 15, +5, +5, +5, +8, +5, 10,
        +5, 10, +8, 13, 15, 12, +3, +3,
    };

    private static final int[] ANCHOR_22 = {
        15, +8, +8, +3, 15, 15, +3, +8,
        15, 15, 15, 15, 15, 15, 15, +8,
        15, +8, 15, +3, 15, +8, 15, +8,
        +3, 15, +6, 10, 15, 15, 10, +8,
        15, +3, 15, 10, 10, +8, +9, 10,
        +6, 15, +8, 15, +3, +6, +6, +8,
        15, +3, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, +3, 15, 15, +8,
    };

    static final int[][] WEIGHTS = {
        {},
        {},
        {0, 21, 43, 64},
        {0, 9, 18, 27, 37, 46, 55, 64},
        {0, 4, 9, 13, 17, 21, 26, 30, 34, 38, 43, 47, 51, 55, 60, 64}
    };

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

    BC7Decoder(PixelOrder order) {
        super(BlockFormat.BC7, order);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine) {
        Bits bits = Bits.from(src, srcPos);

        int modeIndex = Integer.numberOfTrailingZeros(src[srcPos]);
        bits.get(modeIndex + 1); // Skip mode bits
        if (modeIndex >= MODES.size()) {
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
                colors[i * 4 + 3] = 0xff;
            }
        }

        // Let's try a new method
        int ib1 = mode.ib1;
        int ib2 = mode.ib2;
        long indexBits1 = indexBits(bits, mode.ib1, mode.ns, partition);
        long indexBits2 = indexBits(bits, mode.ib2, mode.ns, partition);

        int partitionTable;
        if (mode.ns == 2) {
            partitionTable = SUBSET2[partition];
        } else if (mode.ns == 3) {
            partitionTable = SUBSET3[partition];
        } else {
            partitionTable = 0;
        }

        int[] weights1 = WEIGHTS[ib1];
        int[] weights2 = WEIGHTS[ib2];

        int mask1 = (1 << ib1) - 1;
        int mask2 = (1 << ib2) - 1;
        for (int y = 0, i = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++, i++) {
                int i1 = (int) (indexBits1 & mask1);
                indexBits1 >>>= ib1;
                int cWeight = weights1[i1];
                int aWeight = weights1[i1];

                if (ib2 != 0) {
                    int i2 = (int) (indexBits2 & mask2);
                    if (selection) {
                        cWeight = weights2[i2];
                    } else {
                        aWeight = weights2[i2];
                    }
                    indexBits2 >>>= ib2;
                }

                int pIndex = partitionTable >>> (i * 2) & 3;
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
                dstPos += bytesPerPixel;
            }
            dstPos += bytesPerLine - BLOCK_WIDTH * bytesPerPixel;
        }
    }

    private long indexBits(Bits bits, int ib, int ns, int partition) {
        long indexBits = bits.getLong(ib * 16 - ns);
        indexBits = insertZeroBit(indexBits, ib - 1);

        if (ns == 2) {
            int anchor = ANCHOR_11[partition] + 1;
            indexBits = insertZeroBit(indexBits, (anchor * ib) - 1);
        } else if (ns == 3) {
            int anchor1 = ANCHOR_21[partition] + 1;
            int anchor2 = ANCHOR_22[partition] + 1;
            indexBits = insertZeroBit(indexBits, (Math.min(anchor1, anchor2) * ib) - 1);
            indexBits = insertZeroBit(indexBits, (Math.max(anchor1, anchor2) * ib) - 1);
        }
        return indexBits;
    }

    private long insertZeroBit(long value, int pos) {
        long topMask = ~0L << pos;
        return value + (value & topMask);
    }

    static int interpolate(int e0, int e1, int weight) {
        return (e0 * (64 - weight) + e1 * weight + 32) >>> 6;
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
