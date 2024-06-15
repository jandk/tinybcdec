package be.twofold.tinybcdec;

final class BC4UDecoder extends BCDecoder {
    BC4UDecoder(int bytesPerPixel, int redChannel, int greenChannel, int blueChannel, int alphaChannel) {
        super(8, bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel);
    }

    @Override
    @SuppressWarnings({"PointlessArithmeticExpression", "PointlessBitwiseExpression"})
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);
        int a0 = (int) ((block >>> 0) & 0xff);
        int a1 = (int) ((block >>> 8) & 0xff);
        block >>>= 16;

        byte[] alphas = {(byte) a0, (byte) a1, 0, 0, 0, 0, 0, (byte) 255};
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
        }

        dstPos += redChannel;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                dst[dstPos] = alphas[(int) (block & 7)];
                block >>>= 3;
                dstPos += bytesPerPixel;
            }
            dstPos += stride - BLOCK_WIDTH * bytesPerPixel;
        }
    }
}
