package be.twofold.tinybcdec;

public final class BC3Decoder extends BCDecoder {
    public BC3Decoder() {
        super(16, 4);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        BC1Decoder.decodeColor(src, srcPos + 8, dst, dstPos, stride, true);
        decodeAlpha(src, srcPos, dst, dstPos + 3, stride, 4);
    }

    static void decodeAlpha(byte[] src, int srcPos, byte[] dst, int dstPos, int stride, int size) {
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

        for (int y = 0, shift = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++, shift += 3) {
                int colorIndex = (int) ((block >>> shift) & 7);
                dst[dstPos] = alphas[colorIndex];
                dstPos += size;
            }
            dstPos += stride - 4 * size;
        }
    }
}
