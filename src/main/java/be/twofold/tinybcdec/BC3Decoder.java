package be.twofold.tinybcdec;

public final class BC3Decoder implements BlockDecoder {
    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst) {
        BC1Decoder.decodeColor(src, srcPos + 8, dst, true);
        decodeAlpha(src, srcPos, dst, 3);
    }

    static void decodeAlpha(byte[] src, int srcPos, byte[] dst, int dstPos) {
        long block = ByteArrays.getLong(src, srcPos);
        int a0 = (int) (block & 0xff);
        int a1 = (int) ((block >>> 8) & 0xff);
        block >>>= 16;

        byte[] alphas = new byte[8];
        alphas[0] = (byte) a0;
        alphas[1] = (byte) a1;

        if (a0 > a1) {
            alphas[2] = (byte) ((6 * a0 + 1 * a1 + 3) / 7);
            alphas[3] = (byte) ((5 * a0 + 2 * a1 + 3) / 7);
            alphas[4] = (byte) ((4 * a0 + 3 * a1 + 3) / 7);
            alphas[5] = (byte) ((3 * a0 + 4 * a1 + 3) / 7);
            alphas[6] = (byte) ((2 * a0 + 5 * a1 + 3) / 7);
            alphas[7] = (byte) ((1 * a0 + 6 * a1 + 3) / 7);
        } else {
            alphas[2] = (byte) ((4 * a0 + 1 * a1 + 2) / 5);
            alphas[3] = (byte) ((3 * a0 + 2 * a1 + 2) / 5);
            alphas[4] = (byte) ((2 * a0 + 3 * a1 + 2) / 5);
            alphas[5] = (byte) ((1 * a0 + 4 * a1 + 2) / 5);
            alphas[6] = (byte) 0x00;
            alphas[7] = (byte) 0xff;
        }

        for (int i = 0; i < 16; i++) {
            int index = (int) ((block >>> (i * 3)) & 7);
            dst[(i * 4) + dstPos] = alphas[index];
        }
    }
}
