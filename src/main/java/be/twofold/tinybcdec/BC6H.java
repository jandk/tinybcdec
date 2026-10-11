package be.twofold.tinybcdec;

import java.nio.*;
import java.util.*;

final class BC6H extends BPTC {
    static final int BPP = 8;

    private static final List<Mode> MODES = List.of(
        new Mode(T, T, 10, +5, +5, +5),
        new Mode(T, T, +7, +6, +6, +6),
        new Mode(T, T, 11, +5, +4, +4),
        new Mode(T, T, 11, +4, +5, +4),
        new Mode(T, T, 11, +4, +4, +5),
        new Mode(T, T, +9, +5, +5, +5),
        new Mode(T, T, +8, +6, +5, +5),
        new Mode(T, T, +8, +5, +6, +5),
        new Mode(T, T, +8, +5, +5, +6),
        new Mode(F, T, +6, +6, +6, +6),
        new Mode(F, F, 10, 10, 10, 10),
        new Mode(T, F, 11, +9, +9, +9),
        new Mode(T, F, 12, +8, +8, +8),
        new Mode(T, F, 16, +4, +4, +4)
    );

    private static final byte[] REVERSE_6;

    static {
        REVERSE_6 = new byte[1 << 6];
        for (int i = 0; i < REVERSE_6.length; i++) {
            REVERSE_6[i] = (byte) (Integer.reverse(i) >>> (Integer.SIZE - 6));
        }
    }

    private final Bits bits = new Bits();
    private final int[] endpoints = new int[16];
    private final boolean signed;

    BC6H(boolean signed) {
        super(BPP);
        this.signed = signed;
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        Bits bits = this.bits;
        bits.read(src, srcPos);

        long lo = bits.lo;
        long hi = bits.hi;

        int modeIndex = mode(bits);
        if (modeIndex >= MODES.size()) {
            fillInvalidBlock(dst, dstPos, stride, BPP);
            return;
        }
        Mode mode = MODES.get(modeIndex);

        int[] endpoints = readEndpoints(modeIndex, lo, hi);
        bits.getLong(50);
        bits.getLong(modeIndex < 10 ? modeIndex < 2 ? (75 - 50) : (72 - 50) : (60 - 50));

        int partition = mode.pb ? bits.get(5) : 0;
        int numPartitions = mode.pb ? 2 : 1;

        // The values in E0 are sign-extended to the implementation’s internal integer representation if
        // the format of the texture is signed
        if (signed) {
            endpoints[0] = extendSign(endpoints[0], mode.epb);
            endpoints[1] = extendSign(endpoints[1], mode.epb);
            endpoints[2] = extendSign(endpoints[2], mode.epb);
        }

        if (mode.te || signed) {
            for (int i = 4; i < numPartitions * 8; i += 4) {
                endpoints[i/**/] = extendSign(endpoints[i/**/], mode.rb);
                endpoints[i + 1] = extendSign(endpoints[i + 1], mode.gb);
                endpoints[i + 2] = extendSign(endpoints[i + 2], mode.bb);
            }
        }

        if (mode.te) {
            for (int i = 4; i < numPartitions * 8; i += 4) {
                endpoints[i/**/] = transformInverse(endpoints[i/**/], endpoints[0], mode.epb, signed);
                endpoints[i + 1] = transformInverse(endpoints[i + 1], endpoints[1], mode.epb, signed);
                endpoints[i + 2] = transformInverse(endpoints[i + 2], endpoints[2], mode.epb, signed);
            }
        }

        for (int i = 0; i < numPartitions * 8; i += 4) {
            endpoints[i/**/] = unquantize(endpoints[i/**/], mode.epb, signed);
            endpoints[i + 1] = unquantize(endpoints[i + 1], mode.epb, signed);
            endpoints[i + 2] = unquantize(endpoints[i + 2], mode.epb, signed);
        }

        int ib = mode.pb ? 3 : 4;
        int partitions = partitions(numPartitions, partition);
        long indexBits = indexBits(bits, ib, numPartitions, partition);
        byte[] weights = weights(ib);
        int mask = (1 << ib) - 1;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int weight = weights[(int) (indexBits & mask)];
                indexBits >>>= ib;

                int index = (partitions & 3) * 8;
                long r = finalUnquantize(interpolate(endpoints[index/**/], endpoints[index + 4], weight), signed) & 0xFFFFL;
                long g = finalUnquantize(interpolate(endpoints[index + 1], endpoints[index + 5], weight), signed) & 0xFFFFL;
                long b = finalUnquantize(interpolate(endpoints[index + 2], endpoints[index + 6], weight), signed) & 0xFFFFL;
                partitions >>>= 2;

                int o = dstPos + x * BPP;
                ByteIO.setLong(dst, o, r | g << 16 | b << 32 | 0x3C00_0000_0000_0000L);
            }
            dstPos += stride;
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
                readEndpoints6(lo, hi, endpoints);
                break;
            case 4:
                readEndpoints10(lo, hi, endpoints);
                break;
            case 5:
                readEndpoints14(lo, hi, endpoints);
                break;
            case 6:
                readEndpoints18(lo, hi, endpoints);
                break;
            case 7:
                readEndpoints22(lo, hi, endpoints);
                break;
            case 8:
                readEndpoints26(lo, hi, endpoints);
                break;
            case 9:
                readEndpoints30(lo, hi, endpoints);
                break;
            case 10:
                readEndpoints3(lo, hi, endpoints);
                break;
            case 11:
                readEndpoints7(lo, hi, endpoints);
                break;
            case 12:
                readEndpoints11(lo, hi, endpoints);
                break;
            case 13:
                readEndpoints15(lo, hi, endpoints);
                break;
            default:
                throw new AssertionError();
        }
        return endpoints;
    }

    private int mode(Bits bits) {
        int mode = bits.get(2);
        if (mode < 2) {
            return mode;
        }
        return bits.get(3) + (mode * 8 - 14);
    }

    private void readEndpoints0(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 5) & 1023;
        endpoints[1] = (int) (lo >>> 15) & 1023;
        endpoints[2] = (int) (lo >>> 25) & 1023;
        endpoints[4] = (int) (lo >>> 35) & 31;
        endpoints[5] = (int) (lo >>> 45) & 31;
        endpoints[6] = (int) (lo >>> 55) & 31;
        endpoints[8] = (int) (hi >>> 1) & 31;
        endpoints[9] = (int) (lo << 2) & 16 | (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo << 1) & 16 | (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 31;
        endpoints[13] = (int) (lo >>> 36) & 16 | (int) (lo >>> 51) & 15;
        endpoints[14] = (int) lo & 16 | (int) (hi >>> 9) & 8 | (int) (hi >>> 4) & 4 | (int) (lo >>> 59) & 2 | (int) (lo >>> 50) & 1;
    }

    private void readEndpoints1(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 5) & 127;
        endpoints[1] = (int) (lo >>> 15) & 127;
        endpoints[2] = (int) (lo >>> 25) & 127;
        endpoints[4] = (int) (lo >>> 35) & 63;
        endpoints[5] = (int) (lo >>> 45) & 63;
        endpoints[6] = (int) (lo >>> 55) & 63;
        endpoints[8] = (int) (hi >>> 1) & 63;
        endpoints[9] = (int) (lo << 3) & 32 | (int) (lo >>> 20) & 16 | (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 17) & 32 | (int) (lo >>> 10) & 16 | (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 63;
        endpoints[13] = (int) (lo << 1) & 48 | (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (lo >>> 28) & 32 | (int) (lo >>> 30) & 16 | (int) (lo >>> 29) & 8 | (int) (lo >>> 21) & 4 | (int) (lo >>> 12) & 3;
    }

    private void readEndpoints2(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 30) & 1024 | (int) (lo >>> 5) & 1023;
        endpoints[1] = (int) (lo >>> 39) & 1024 | (int) (lo >>> 15) & 1023;
        endpoints[2] = (int) (lo >>> 49) & 1024 | (int) (lo >>> 25) & 1023;
        endpoints[4] = (int) (lo >>> 35) & 31;
        endpoints[5] = (int) (lo >>> 45) & 15;
        endpoints[6] = (int) (lo >>> 55) & 15;
        endpoints[8] = (int) (hi >>> 1) & 31;
        endpoints[9] = (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 31;
        endpoints[13] = (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (hi >>> 9) & 8 | (int) (hi >>> 4) & 4 | (int) (lo >>> 59) & 2 | (int) (lo >>> 50) & 1;
    }

    private void readEndpoints6(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 29) & 1024 | (int) (lo >>> 5) & 1023;
        endpoints[1] = (int) (lo >>> 40) & 1024 | (int) (lo >>> 15) & 1023;
        endpoints[2] = (int) (lo >>> 49) & 1024 | (int) (lo >>> 25) & 1023;
        endpoints[4] = (int) (lo >>> 35) & 15;
        endpoints[5] = (int) (lo >>> 45) & 31;
        endpoints[6] = (int) (lo >>> 55) & 15;
        endpoints[8] = (int) (hi >>> 1) & 15;
        endpoints[9] = (int) (hi >>> 7) & 16 | (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 15;
        endpoints[13] = (int) (lo >>> 36) & 16 | (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (hi >>> 9) & 8 | (int) (hi >>> 4) & 4 | (int) (lo >>> 59) & 2 | (int) (hi >>> 5) & 1;
    }

    private void readEndpoints10(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 29) & 1024 | (int) (lo >>> 5) & 1023;
        endpoints[1] = (int) (lo >>> 39) & 1024 | (int) (lo >>> 15) & 1023;
        endpoints[2] = (int) (lo >>> 50) & 1024 | (int) (lo >>> 25) & 1023;
        endpoints[4] = (int) (lo >>> 35) & 15;
        endpoints[5] = (int) (lo >>> 45) & 15;
        endpoints[6] = (int) (lo >>> 55) & 31;
        endpoints[8] = (int) (hi >>> 1) & 15;
        endpoints[9] = (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 36) & 16 | (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 15;
        endpoints[13] = (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (hi >>> 7) & 16 | (int) (hi >>> 9) & 8 | (int) (hi >>> 4) & 6 | (int) (lo >>> 50) & 1;
    }

    private void readEndpoints14(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 5) & 511;
        endpoints[1] = (int) (lo >>> 15) & 511;
        endpoints[2] = (int) (lo >>> 25) & 511;
        endpoints[4] = (int) (lo >>> 35) & 31;
        endpoints[5] = (int) (lo >>> 45) & 31;
        endpoints[6] = (int) (lo >>> 55) & 31;
        endpoints[8] = (int) (hi >>> 1) & 31;
        endpoints[9] = (int) (lo >>> 20) & 16 | (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 10) & 16 | (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 31;
        endpoints[13] = (int) (lo >>> 36) & 16 | (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (lo >>> 30) & 16 | (int) (hi >>> 9) & 8 | (int) (hi >>> 4) & 4 | (int) (lo >>> 59) & 2 | (int) (lo >>> 50) & 1;
    }

    private void readEndpoints18(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 5) & 255;
        endpoints[1] = (int) (lo >>> 15) & 255;
        endpoints[2] = (int) (lo >>> 25) & 255;
        endpoints[4] = (int) (lo >>> 35) & 63;
        endpoints[5] = (int) (lo >>> 45) & 31;
        endpoints[6] = (int) (lo >>> 55) & 31;
        endpoints[8] = (int) (hi >>> 1) & 63;
        endpoints[9] = (int) (lo >>> 20) & 16 | (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 10) & 16 | (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 63;
        endpoints[13] = (int) (lo >>> 9) & 16 | (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (lo >>> 30) & 24 | (int) (lo >>> 21) & 4 | (int) (lo >>> 59) & 2 | (int) (lo >>> 50) & 1;
    }

    private void readEndpoints22(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 5) & 255;
        endpoints[1] = (int) (lo >>> 15) & 255;
        endpoints[2] = (int) (lo >>> 25) & 255;
        endpoints[4] = (int) (lo >>> 35) & 31;
        endpoints[5] = (int) (lo >>> 45) & 63;
        endpoints[6] = (int) (lo >>> 55) & 31;
        endpoints[8] = (int) (hi >>> 1) & 31;
        endpoints[9] = (int) (lo >>> 18) & 32 | (int) (lo >>> 20) & 16 | (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 10) & 16 | (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 31;
        endpoints[13] = (int) (lo >>> 28) & 32 | (int) (lo >>> 36) & 16 | (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (lo >>> 30) & 16 | (int) (hi >>> 9) & 8 | (int) (hi >>> 4) & 4 | (int) (lo >>> 59) & 2 | (int) (lo >>> 13) & 1;
    }

    private void readEndpoints26(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 5) & 255;
        endpoints[1] = (int) (lo >>> 15) & 255;
        endpoints[2] = (int) (lo >>> 25) & 255;
        endpoints[4] = (int) (lo >>> 35) & 31;
        endpoints[5] = (int) (lo >>> 45) & 31;
        endpoints[6] = (int) (lo >>> 55) & 63;
        endpoints[8] = (int) (hi >>> 1) & 31;
        endpoints[9] = (int) (lo >>> 20) & 16 | (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 18) & 32 | (int) (lo >>> 10) & 16 | (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 31;
        endpoints[13] = (int) (lo >>> 36) & 16 | (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (lo >>> 28) & 32 | (int) (lo >>> 30) & 16 | (int) (hi >>> 9) & 8 | (int) (hi >>> 4) & 4 | (int) (lo >>> 12) & 2 | (int) (lo >>> 50) & 1;
    }

    private void readEndpoints30(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 5) & 63;
        endpoints[1] = (int) (lo >>> 15) & 63;
        endpoints[2] = (int) (lo >>> 25) & 63;
        endpoints[4] = (int) (lo >>> 35) & 63;
        endpoints[5] = (int) (lo >>> 45) & 63;
        endpoints[6] = (int) (lo >>> 55) & 63;
        endpoints[8] = (int) (hi >>> 1) & 63;
        endpoints[9] = (int) (lo >>> 16) & 32 | (int) (lo >>> 20) & 16 | (int) (lo >>> 41) & 15;
        endpoints[10] = (int) (lo >>> 17) & 32 | (int) (lo >>> 10) & 16 | (int) (lo >>> 61) | (int) (hi << 3) & 8;
        endpoints[12] = (int) (hi >>> 7) & 63;
        endpoints[13] = (int) (lo >>> 26) & 32 | (int) (lo >>> 7) & 16 | (int) (lo >>> 51) & 15;
        endpoints[14] = (int) (lo >>> 28) & 32 | (int) (lo >>> 30) & 16 | (int) (lo >>> 29) & 8 | (int) (lo >>> 21) & 4 | (int) (lo >>> 12) & 3;
    }

    private void readEndpoints3(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 5) & 1023;
        endpoints[1] = (int) (lo >>> 15) & 1023;
        endpoints[2] = (int) (lo >>> 25) & 1023;
        endpoints[4] = (int) (lo >>> 35) & 1023;
        endpoints[5] = (int) (lo >>> 45) & 1023;
        endpoints[6] = (int) (lo >>> 55) | (int) (hi << 9) & 512;
    }

    private void readEndpoints7(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 34) & 1024 | (int) (lo >>> 5) & 1023;
        endpoints[1] = (int) (lo >>> 44) & 1024 | (int) (lo >>> 15) & 1023;
        endpoints[2] = (int) (hi << 10) & 1024 | (int) (lo >>> 25) & 1023;
        endpoints[4] = (int) (lo >>> 35) & 511;
        endpoints[5] = (int) (lo >>> 45) & 511;
        endpoints[6] = (int) (lo >>> 55);
    }

    private void readEndpoints11(long lo, long hi, int[] endpoints) {
        endpoints[0] = (int) (lo >>> 32) & 2048 | (int) (lo >>> 34) & 1024 | (int) (lo >>> 5) & 1023;
        endpoints[1] = (int) (lo >>> 42) & 2048 | (int) (lo >>> 44) & 1024 | (int) (lo >>> 15) & 1023;
        endpoints[2] = (int) (lo >>> 52) & 2048 | (int) (hi << 10) & 1024 | (int) (lo >>> 25) & 1023;
        endpoints[4] = (int) (lo >>> 35) & 255;
        endpoints[5] = (int) (lo >>> 45) & 255;
        endpoints[6] = (int) (lo >>> 55) & 255;
    }

    private void readEndpoints15(long lo, long hi, int[] endpoints) {
        endpoints[0] = REVERSE_6[(int) (lo >>> 39) & 63] << 10 | (int) (lo >>> 5) & 1023;
        endpoints[1] = REVERSE_6[(int) (lo >>> 49) & 63] << 10 | (int) (lo >>> 15) & 1023;
        endpoints[2] = REVERSE_6[(int) (lo >>> 59) | (int) (hi << 5) & 32] << 10 | (int) (lo >>> 25) & 1023;
        endpoints[4] = (int) (lo >>> 35) & 15;
        endpoints[5] = (int) (lo >>> 45) & 15;
        endpoints[6] = (int) (lo >>> 55) & 15;
    }

    private int unquantize(int value, int bits, boolean signed) {
        if (signed) {
            return unquantizeSigned(value, bits);
        } else {
            return unquantizeUnsigned(value, bits);
        }
    }

    private int unquantizeUnsigned(int value, int bits) {
        if (bits >= 15 || value == 0) {
            return value;
        }
        if (value == ((1 << bits) - 1)) {
            return 0xFFFF;
        }
        return ((value << 15) + 0x4000) >>> (bits - 1);
    }

    private int unquantizeSigned(int value, int bits) {
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

    private short finalUnquantize(int i, boolean signed) {
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

    private static final class Mode {
        private final boolean te;
        private final boolean pb;
        private final byte epb;
        private final byte rb;
        private final byte gb;
        private final byte bb;

        private Mode(boolean te, boolean pb, int epb, int rb, int gb, int bb) {
            this.te = te;
            this.pb = pb;
            this.epb = (byte) epb;
            this.rb = (byte) rb;
            this.gb = (byte) gb;
            this.bb = (byte) bb;
        }
    }
}
