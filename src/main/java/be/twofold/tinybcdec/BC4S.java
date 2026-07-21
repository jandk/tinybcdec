package be.twofold.tinybcdec;

import java.nio.*;

final class BC4S extends BlockDecoder {
    private static final int BPP = 4;

    private final byte[] alphas = new byte[8];

    BC4S() {
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
        int a0 = Math.max(-127, (byte) (block/*  */));
        int a1 = Math.max(-127, (byte) (block >>> 8));

        alphas[0] = scale127(a0);
        alphas[1] = scale127(a1);

        if (a0 > a1) {
            alphas[2] = scale889(6 * a0 + /**/a1);
            alphas[3] = scale889(5 * a0 + 2 * a1);
            alphas[4] = scale889(4 * a0 + 3 * a1);
            alphas[5] = scale889(3 * a0 + 4 * a1);
            alphas[6] = scale889(2 * a0 + 5 * a1);
            alphas[7] = scale889(/**/a0 + 6 * a1);
        } else {
            alphas[2] = scale635(4 * a0 + /**/a1);
            alphas[3] = scale635(3 * a0 + 2 * a1);
            alphas[4] = scale635(2 * a0 + 3 * a1);
            alphas[5] = scale635(/**/a0 + 4 * a1);
            alphas[6] = (byte) 0x00;
            alphas[7] = (byte) 0xFF;
        }

        return block >>> 16;
    }

    private static byte scale127(int i) {
        return (byte) ((i * 129 + 16384) >>> 7);
    }

    private static byte scale889(int i) {
        return (byte) ((i * 75193 + 67108864) >>> 19);
    }

    private static byte scale635(int i) {
        return (byte) ((i * 13159 + 8388708) >>> 16);
    }
}
