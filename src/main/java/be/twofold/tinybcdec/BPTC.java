package be.twofold.tinybcdec;

abstract class BPTC extends BlockDecoder {
    private static final int[][] PARTITIONS = {
        {},
        {0},
        {
            0x50505050, 0x40404040, 0x54545454, 0x54505040, 0x50404000, 0x55545450, 0x55545040, 0x54504000,
            0x50400000, 0x55555450, 0x55544000, 0x54400000, 0x55555440, 0x55550000, 0x55555500, 0x55000000,
            0x55150100, 0x00004054, 0x15010000, 0x00405054, 0x00004050, 0x15050100, 0x05010000, 0x40505054,
            0x00404050, 0x05010100, 0x14141414, 0x05141450, 0x01155440, 0x00555500, 0x15014054, 0x05414150,
            0x44444444, 0x55005500, 0x11441144, 0x05055050, 0x05500550, 0x11114444, 0x41144114, 0x44111144,
            0x15055054, 0x01055040, 0x05041050, 0x05455150, 0x14414114, 0x50050550, 0x41411414, 0x00141400,
            0x00041504, 0x00105410, 0x10541000, 0x04150400, 0x50410514, 0x41051450, 0x05415014, 0x14054150,
            0x41050514, 0x41505014, 0x40011554, 0x54150140, 0x50505500, 0x00555050, 0x15151010, 0x54540404,
        },
        {
            0xAA685050, 0x6A5A5040, 0x5A5A4200, 0x5450A0A8, 0xA5A50000, 0xA0A05050, 0x5555A0A0, 0x5A5A5050,
            0xAA550000, 0xAA555500, 0xAAAA5500, 0x90909090, 0x94949494, 0xA4A4A4A4, 0xA9A59450, 0x2A0A4250,
            0xA5945040, 0x0A425054, 0xA5A5A500, 0x55A0A0A0, 0xA8A85454, 0x6A6A4040, 0xA4A45000, 0x1A1A0500,
            0x0050A4A4, 0xAAA59090, 0x14696914, 0x69691400, 0xA08585A0, 0xAA821414, 0x50A4A450, 0x6A5A0200,
            0xA9A58000, 0x5090A0A8, 0xA8A09050, 0x24242424, 0x00AA5500, 0x24924924, 0x24499224, 0x50A50A50,
            0x500AA550, 0xAAAA4444, 0x66660000, 0xA5A0A5A0, 0x50A050A0, 0x69286928, 0x44AAAA44, 0x66666600,
            0xAA444444, 0x54A854A8, 0x95809580, 0x96969600, 0xA85454A8, 0x80959580, 0xAA141414, 0x96960000,
            0xAAAA1414, 0xA05050A0, 0xA0A5A5A0, 0x96000000, 0x40804080, 0xA9A8A9A8, 0xAAAAAA44, 0x2A4A5254,
        },
    };

    private static final byte[] ANCHOR_11 = {
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, +2, +8, +2, +2, +8, +8, 15,
        +2, +8, +2, +2, +8, +8, +2, +2,
        15, 15, +6, +8, +2, +8, 15, 15,
        +2, +8, +2, +2, +2, 15, 15, +6,
        +6, +2, +6, +8, 15, 15, +2, +2,
        15, 15, 15, 15, 15, +2, +2, 15,
    };

    private static final byte[] ANCHOR_21 = {
        +3, +3, 15, 15, +8, +3, 15, 15,
        +8, +8, +6, +6, +6, +5, +3, +3,
        +3, +3, +8, 15, +3, +3, +6, 10,
        +5, +8, +8, +6, +8, +5, 15, 15,
        +8, 15, +3, +5, +6, 10, +8, 15,
        15, +3, 15, +5, 15, 15, 15, 15,
        +3, 15, +5, +5, +5, +8, +5, 10,
        +5, 10, +8, 13, 15, 12, +3, +3,
    };

    private static final byte[] ANCHOR_22 = {
        15, +8, +8, +3, 15, 15, +3, +8,
        15, 15, 15, 15, 15, 15, 15, +8,
        15, +8, 15, +3, 15, +8, 15, +8,
        +3, 15, +6, 10, 15, 15, 10, +8,
        15, +3, 15, 10, 10, +8, +9, 10,
        +6, 15, +8, 15, +3, +6, +6, +8,
        15, +3, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, +3, 15, 15, +8,
    };

    private static final byte[][] WEIGHTS = {
        {},
        {},
        {0, 21, 43, 64},
        {0, 9, 18, 27, 37, 46, 55, 64},
        {0, 4, 9, 13, 17, 21, 26, 30, 34, 38, 43, 47, 51, 55, 60, 64},
    };

    static final boolean T = true;
    static final boolean F = false;

    BPTC(int pixelStride) {
        super(pixelStride, 16);
    }

    static int partitions(int numPartitions, int partitionIndex) {
        return PARTITIONS[numPartitions][partitionIndex];
    }

    static byte[] weights(int numIndexBits) {
        return WEIGHTS[numIndexBits];
    }

    static long indexBits(Bits bits, int numIndexBits, int numPartitions, int partition) {
        long indexBits = bits.getLong(numIndexBits * 16 - numPartitions);
        indexBits = insertZeroBit(indexBits, numIndexBits - 1);

        if (numPartitions == 2) {
            int anchor = ANCHOR_11[partition];
            indexBits = insertZeroBit(indexBits, ((anchor + 1) * numIndexBits) - 1);
        } else if (numPartitions == 3) {
            int anchor1 = ANCHOR_21[partition];
            int anchor2 = ANCHOR_22[partition];
            indexBits = insertZeroBit(indexBits, ((Math.min(anchor1, anchor2) + 1) * numIndexBits) - 1);
            indexBits = insertZeroBit(indexBits, ((Math.max(anchor1, anchor2) + 1) * numIndexBits) - 1);
        }
        return indexBits;
    }

    private static long insertZeroBit(long value, int pos) {
        return value + (value & (~0L << pos));
    }

    static int interpolate(int e0, int e1, int weight) {
        return (e0 * (64 - weight) + e1 * weight + 32) >>> 6;
    }
}
