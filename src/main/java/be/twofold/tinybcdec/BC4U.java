package be.twofold.tinybcdec;

import java.nio.*;

final class BC4U extends BlockDecoder {
    static final int BPP = 4;

    private final byte[] alphas = new byte[8];

    BC4U() {
        super(BPP, 8);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        byte[] alphas = this.alphas;
        long indices = buildAlphas(ByteIO.getLong(src, srcPos), alphas);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int alpha = alphas[(int) (indices & 0x07)] & 0xFF;
                ByteIO.setInt(dst, dstPos + x * BPP, alpha * 0x01_0101 | 0xFF00_0000);
                indices >>>= 3;
            }
            dstPos += stride;
        }
    }

    static long buildAlphas(long block, byte[] alphas) {
        int a0 = (int) (block/*  */) & 0xFF;
        int a1 = (int) (block >>> 8) & 0xFF;

        alphas[0] = (byte) a0;
        alphas[1] = (byte) a1;

        if (a0 > a1) {
            alphas[2] = scale1785(6 * a0 + /**/a1);
            alphas[3] = scale1785(5 * a0 + 2 * a1);
            alphas[4] = scale1785(4 * a0 + 3 * a1);
            alphas[5] = scale1785(3 * a0 + 4 * a1);
            alphas[6] = scale1785(2 * a0 + 5 * a1);
            alphas[7] = scale1785(/**/a0 + 6 * a1);
        } else {
            alphas[2] = scale1275(4 * a0 + /**/a1);
            alphas[3] = scale1275(3 * a0 + 2 * a1);
            alphas[4] = scale1275(2 * a0 + 3 * a1);
            alphas[5] = scale1275(/**/a0 + 4 * a1);
            alphas[6] = (byte) 0x00;
            alphas[7] = (byte) 0xFF;
        }

        return block >>> 16;
    }

    private static byte scale1785(int i) {
        return (byte) ((i * 585 + 2010) >>> 12);
    }

    private static byte scale1275(int i) {
        return (byte) ((i * 819 + 1893) >>> 12);
    }
}
