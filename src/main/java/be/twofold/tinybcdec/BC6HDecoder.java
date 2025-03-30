package be.twofold.tinybcdec;

import java.util.*;

final class BC6HDecoder extends BPTCDecoder {
    private static final int BPP = 6;

    private static final List<Mode> MODES = List.of(
        new Mode(true, +5, 10, +5, +5, +5, new short[]{0x0741, 0x0841, 0x0B41, 0x000A, 0x010A, 0x020A, 0x0305, 0x0A41, 0x0704, 0x0405, 0x0B01, 0x0A04, 0x0505, 0x0B11, 0x0804, 0x0605, 0x0B21, 0x0905, 0x0B31}),
        new Mode(true, +5, +7, +6, +6, +6, new short[]{0x0751, 0x0A41, 0x0A51, 0x0007, 0x0B01, 0x0B11, 0x0841, 0x0107, 0x0851, 0x0B21, 0x0741, 0x0207, 0x0B31, 0x0B51, 0x0B41, 0x0306, 0x0704, 0x0406, 0x0A04, 0x0506, 0x0804, 0x0606, 0x0906}),
        new Mode(true, +5, 11, +5, +4, +4, new short[]{0x000A, 0x010A, 0x020A, 0x0305, 0x00A1, 0x0704, 0x0404, 0x01A1, 0x0B01, 0x0A04, 0x0504, 0x02A1, 0x0B11, 0x0804, 0x0605, 0x0B21, 0x0905, 0x0B31}),
        new Mode(true, +5, 11, +4, +5, +4, new short[]{0x000A, 0x010A, 0x020A, 0x0304, 0x00A1, 0x0A41, 0x0704, 0x0405, 0x01A1, 0x0A04, 0x0504, 0x02A1, 0x0B11, 0x0804, 0x0604, 0x0B01, 0x0B21, 0x0904, 0x0741, 0x0B31}),
        new Mode(true, +5, 11, +4, +4, +5, new short[]{0x000A, 0x010A, 0x020A, 0x0304, 0x00A1, 0x0841, 0x0704, 0x0404, 0x01A1, 0x0B01, 0x0A04, 0x0505, 0x02A1, 0x0804, 0x0604, 0x0B11, 0x0B21, 0x0904, 0x0B41, 0x0B31}),
        new Mode(true, +5, +9, +5, +5, +5, new short[]{0x0009, 0x0841, 0x0109, 0x0741, 0x0209, 0x0B41, 0x0305, 0x0A41, 0x0704, 0x0405, 0x0B01, 0x0A04, 0x0505, 0x0B11, 0x0804, 0x0605, 0x0B21, 0x0905, 0x0B31}),
        new Mode(true, +5, +8, +6, +5, +5, new short[]{0x0008, 0x0A41, 0x0841, 0x0108, 0x0B21, 0x0741, 0x0208, 0x0B31, 0x0B41, 0x0306, 0x0704, 0x0405, 0x0B01, 0x0A04, 0x0505, 0x0B11, 0x0804, 0x0606, 0x0906}),
        new Mode(true, +5, +8, +5, +6, +5, new short[]{0x0008, 0x0B01, 0x0841, 0x0108, 0x0751, 0x0741, 0x0208, 0x0A51, 0x0B41, 0x0305, 0x0A41, 0x0704, 0x0406, 0x0A04, 0x0505, 0x0B11, 0x0804, 0x0605, 0x0B21, 0x0905, 0x0B31}),
        new Mode(true, +5, +8, +5, +5, +6, new short[]{0x0008, 0x0B11, 0x0841, 0x0108, 0x0851, 0x0741, 0x0208, 0x0B51, 0x0B41, 0x0305, 0x0A41, 0x0704, 0x0405, 0x0B01, 0x0A04, 0x0506, 0x0804, 0x0605, 0x0B21, 0x0905, 0x0B31}),
        new Mode(false, 5, +6, +6, +6, +6, new short[]{0x0006, 0x0A41, 0x0B01, 0x0B11, 0x0841, 0x0106, 0x0751, 0x0851, 0x0B21, 0x0741, 0x0206, 0x0A51, 0x0B31, 0x0B51, 0x0B41, 0x0306, 0x0704, 0x0406, 0x0A04, 0x0506, 0x0804, 0x0606, 0x0906}),
        new Mode(false, 0, 10, 10, 10, 10, new short[]{0x000A, 0x010A, 0x020A, 0x030A, 0x040A, 0x050A}),
        new Mode(true, +0, 11, +9, +9, +9, new short[]{0x000A, 0x010A, 0x020A, 0x0309, 0x00A1, 0x0409, 0x01A1, 0x0509, 0x02A1}),
        new Mode(true, +0, 12, +8, +8, +8, new short[]{0x000A, 0x010A, 0x020A, 0x0308, 0x10A2, 0x0408, 0x11A2, 0x0508, 0x12A2}),
        new Mode(true, +0, 16, +4, +4, +4, new short[]{0x000A, 0x010A, 0x020A, 0x0304, 0x10A6, 0x0404, 0x11A6, 0x0504, 0x12A6})
    );

    private final boolean signed;
    private final PixelWriter writer;

    BC6HDecoder(boolean signed, boolean asFloat) {
        super(signed ? BlockFormat.BC6H_SF16 : BlockFormat.BC6H_UF16, asFloat ? 12 : 6);
        this.signed = signed;
        this.writer = PixelWriter.create(asFloat);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        Bits bits = Bits.from(src, srcPos);
        int modeIndex = mode(bits);
        if (modeIndex >= MODES.size()) {
            fillInvalidBlock(dst, dstPos, stride);
            return;
        }

        Mode mode = MODES.get(modeIndex);
        int[] colors = new int[12];

        for (short op : mode.ops) {
            readOp(bits, op, colors);
        }

        int partition = bits.get(mode.pb);
        int numPartitions = mode.pb == 0 ? 1 : 2;

        // The values in E0 are sign-extended to the implementation’s internal integer representation if
        // the format of the texture is signed
        if (signed) {
            colors[0] = extendSign(colors[0], mode.epb);
            colors[1] = extendSign(colors[1], mode.epb);
            colors[2] = extendSign(colors[2], mode.epb);
        }

        if (mode.te || signed) {
            for (int i = 3; i < numPartitions * 6; i += 3) {
                colors[i/**/] = extendSign(colors[i/**/], mode.rb);
                colors[i + 1] = extendSign(colors[i + 1], mode.gb);
                colors[i + 2] = extendSign(colors[i + 2], mode.bb);
            }
        }

        if (mode.te) {
            for (int i = 3; i < numPartitions * 6; i += 3) {
                colors[i/**/] = transformInverse(colors[i/**/], colors[0], mode.epb, signed);
                colors[i + 1] = transformInverse(colors[i + 1], colors[1], mode.epb, signed);
                colors[i + 2] = transformInverse(colors[i + 2], colors[2], mode.epb, signed);
            }
        }

        for (int i = 0; i < numPartitions * 6; i += 3) {
            colors[i/**/] = unquantize(colors[i/**/], mode.epb, signed);
            colors[i + 1] = unquantize(colors[i + 1], mode.epb, signed);
            colors[i + 2] = unquantize(colors[i + 2], mode.epb, signed);
        }

        int ib = numPartitions == 1 ? 4 : 3;
        int partitions = partitions(numPartitions, partition);
        long indexBits = indexBits(bits, ib, numPartitions, partition);
        byte[] weights = weights(ib);
        int mask = (1 << ib) - 1;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int weight = weights[(int) (indexBits & mask)];
                indexBits >>>= ib;

                int pIndex = partitions & 3;
                short r = finalUnquantize(interpolate(colors[pIndex * 6/**/], colors[pIndex * 6 + 3], weight), signed);
                short g = finalUnquantize(interpolate(colors[pIndex * 6 + 1], colors[pIndex * 6 + 4], weight), signed);
                short b = finalUnquantize(interpolate(colors[pIndex * 6 + 2], colors[pIndex * 6 + 5], weight), signed);
                partitions >>>= 2;

                writer.write(dst, dstPos + x * pixelStride, r, g, b);
            }
            dstPos += stride;
        }
    }

    private void readOp(Bits bits, short op, int[] colors) {
        // count | shift << 4 | index << 8 | (reverse ? 1 : 0) << 12
        int count = op & 0x0F;
        int shift = (op >>> 4) & 0x0F;
        int index = (op >>> 8) & 0x0F;
        boolean reverse = (op >>> 12) != 0;

        int value = bits.get(count);
        if (reverse) {
            value = Integer.reverse(value) >>> (32 - count);
        }
        colors[index] |= value << shift;
    }

    private int mode(Bits bits) {
        int mode = bits.get(2);
        if (mode < 2) {
            return mode;
        }
        return bits.get(3) + (mode * 8 - 14);
    }

    private int unquantize(int value, int bits, boolean signed) {
        if (signed) {
            return unquantizeSigned(value, bits);
        } else {
            return unquantizeUnsigned(value, bits);
        }
    }

    private static int unquantizeUnsigned(int value, int bits) {
        if (bits >= 15 || value == 0) {
            return value;
        }
        if (value == ((1 << bits) - 1)) {
            return 0xFFFF;
        }
        return ((value << 15) + 0x4000) >>> (bits - 1);
    }

    private static int unquantizeSigned(int value, int bits) {
        if (bits >= 16 || value == 0) {
            return value;
        }

        boolean sign;
        if (value < 0) {
            value = -value;
            sign = true;
        } else {
            sign = false;
        }

        int unq;
        if (value >= ((1 << (bits - 1)) - 1)) {
            unq = 0x7FFF;
        } else {
            unq = ((value << 15) + 0x4000) >> (bits - 1);
        }
        return sign ? -unq : unq;
    }

    private static short finalUnquantize(int i, boolean signed) {
        if (signed) {
            i = (short) i;
            return (short) (i < 0 ? (((-i) * 31) >> 5) + 0x8000 : (i * 31) >> 5);
        } else {
            return (short) ((i * 31) >> 6);
        }
    }

    private int extendSign(int value, int bits) {
        int signBit = 1 << (bits - 1);
        return (value ^ signBit) - signBit;
    }

    private int transformInverse(int value, int value0, int bits, boolean signed) {
        value = (value + value0) & ((1 << bits) - 1);
        return signed ? extendSign(value, bits) : value;
    }

    private static void fillInvalidBlock(byte[] dst, int dstPos, int stride) {
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            Arrays.fill(dst, dstPos, dstPos + 4 * BPP, (byte) 0);
            dstPos += stride;
        }
    }

    private static final class Mode {
        private final boolean te;
        private final byte pb;
        private final byte epb;
        private final byte rb;
        private final byte gb;
        private final byte bb;
        private final short[] ops;

        private Mode(boolean te, int pb, int epb, int rb, int gb, int bb, short[] ops) {
            this.te = te;
            this.pb = (byte) pb;
            this.epb = (byte) epb;
            this.rb = (byte) rb;
            this.gb = (byte) gb;
            this.bb = (byte) bb;
            this.ops = ops;
        }
    }

    @FunctionalInterface
    private interface PixelWriter {
        void write(byte[] dst, int dstOffset, short r, short g, short b);

        static PixelWriter create(boolean asFloat) {
            if (asFloat) {
                return PixelWriter::writePixelF32;
            } else {
                return PixelWriter::writePixelF16;
            }
        }

        private static void writePixelF32(byte[] dst, int dstPos, short r, short g, short b) {
            ByteArrays.setFloat(dst, dstPos/**/, Platform.float16ToFloat(r));
            ByteArrays.setFloat(dst, dstPos + 4, Platform.float16ToFloat(g));
            ByteArrays.setFloat(dst, dstPos + 8, Platform.float16ToFloat(b));
        }

        private static void writePixelF16(byte[] dst, int dstPos, short r, short g, short b) {
            ByteArrays.setShort(dst, dstPos/**/, r);
            ByteArrays.setShort(dst, dstPos + 2, g);
            ByteArrays.setShort(dst, dstPos + 4, b);
        }
    }
}
