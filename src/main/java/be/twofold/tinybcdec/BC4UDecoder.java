package be.twofold.tinybcdec;

final class BC4UDecoder extends BlockDecoder {
    BC4UDecoder(int pixelStride) {
        super(BlockFormat.BC4U, pixelStride);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        int a0 = (int)  block        & 0xFF;
        int a1 = (int) (block >>> 8) & 0xFF;

        byte[] alphas = {(byte) a0, (byte) a1, 0, 0, 0, 0, 0, (byte) 0xFF};
        if (a0 > a1) {
            alphas[2] = scale1785(6 * a0 +     a1);
            alphas[3] = scale1785(5 * a0 + 2 * a1);
            alphas[4] = scale1785(4 * a0 + 3 * a1);
            alphas[5] = scale1785(3 * a0 + 4 * a1);
            alphas[6] = scale1785(2 * a0 + 5 * a1);
            alphas[7] = scale1785(    a0 + 6 * a1);
        } else {
            alphas[2] = scale1275(4 * a0 +     a1);
            alphas[3] = scale1275(3 * a0 + 2 * a1);
            alphas[4] = scale1275(2 * a0 + 3 * a1);
            alphas[5] = scale1275(    a0 + 4 * a1);
        }
        // @formatter:on

        long indices = block >>> 16;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                byte alpha = alphas[(int) (indices & 0x07)];
                dst[dstPos + x * pixelStride] = alpha;
                indices >>>= 3;
            }
            dstPos += stride;
        }
    }

    private static byte scale1785(int i) {
        return (byte) ((i * 585 + 2010) >>> 12);
    }

    private static byte scale1275(int i) {
        return (byte) ((i * 819 + 1893) >>> 12);
    }
}
