package be.twofold.tinybcdec;

import java.nio.*;

final class BC5S extends BlockDecoder {
    private static final int BPP = 4;

    BC5S() {
        super(BPP, 16);
    }

    /**
     * This method of packing all alphas into a long is slower on BC4, but faster on BC5. Go figure.
     */
    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        long rBlock = ByteIO.getLong(src, srcPos/**/);
        long gBlock = ByteIO.getLong(src, srcPos + 8);

        long rAlphas = buildAlphas(rBlock);
        long gAlphas = buildAlphas(gBlock);
        long rIndices = rBlock >>> 16;
        long gIndices = gBlock >>> 16;

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int rAlpha = (int) (rAlphas >>> (((int) rIndices & 0x07) << 3)) & 0xFF;
                int gAlpha = (int) (gAlphas >>> (((int) gIndices & 0x07) << 3)) & 0xFF;
                ByteIO.setInt(dst, dstPos + x * BPP, gAlpha << 8 | rAlpha << 16 | 0xFF00_0000);
                rIndices >>>= 3;
                gIndices >>>= 3;
            }
            dstPos += stride;
        }
    }

    private static long buildAlphas(long block) {
        int a0 = Math.max(-127, (byte) (block/*  */));
        int a1 = Math.max(-127, (byte) (block >>> 8));

        long alphas = BC4S.scale127(a0) | BC4S.scale127(a1) << 8;
        if (a0 > a1) {
            return alphas
                | (long) BC4S.scale889(6 * a0 + /**/a1) << 16
                | (long) BC4S.scale889(5 * a0 + 2 * a1) << 24
                | (long) BC4S.scale889(4 * a0 + 3 * a1) << 32
                | (long) BC4S.scale889(3 * a0 + 4 * a1) << 40
                | (long) BC4S.scale889(2 * a0 + 5 * a1) << 48
                | (long) BC4S.scale889(/**/a0 + 6 * a1) << 56;
        } else {
            return alphas
                | (long) BC4S.scale635(4 * a0 + /**/a1) << 16
                | (long) BC4S.scale635(3 * a0 + 2 * a1) << 24
                | (long) BC4S.scale635(2 * a0 + 3 * a1) << 32
                | (long) BC4S.scale635(/**/a0 + 4 * a1) << 40
                | 0xFF00_0000_0000_0000L;
        }
    }
}
