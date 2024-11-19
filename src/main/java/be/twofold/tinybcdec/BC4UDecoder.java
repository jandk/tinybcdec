package be.twofold.tinybcdec;

final class BC4UDecoder extends BlockDecoder {
    BC4UDecoder(int bytesPerPixel) {
        super(BlockFormat.BC4Unsigned, bytesPerPixel);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        int a0 = (int) ( block        & 0xFF);
        int a1 = (int) ((block >>> 8) & 0xFF);

        byte[] alphas = {(byte) a0, (byte) a1, 0, 0, 0, 0, 0, (byte) 0xFF};
        if (a0 > a1) {
            alphas[2] = (byte) ((6 * a0 +     a1 + 3) / 7);
            alphas[3] = (byte) ((5 * a0 + 2 * a1 + 3) / 7);
            alphas[4] = (byte) ((4 * a0 + 3 * a1 + 3) / 7);
            alphas[5] = (byte) ((3 * a0 + 4 * a1 + 3) / 7);
            alphas[6] = (byte) ((2 * a0 + 5 * a1 + 3) / 7);
            alphas[7] = (byte) ((    a0 + 6 * a1 + 3) / 7);
        } else {
            alphas[2] = (byte) ((4 * a0 +     a1 + 2) / 5);
            alphas[3] = (byte) ((3 * a0 + 2 * a1 + 2) / 5);
            alphas[4] = (byte) ((2 * a0 + 3 * a1 + 2) / 5);
            alphas[5] = (byte) ((    a0 + 4 * a1 + 2) / 5);
        }
        // @formatter:on

        long indices = block >>> 16;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int index = (int) (indices & 0x07);
                dst[dstPos + x * bytesPerPixel] = alphas[index];
                indices >>>= 3;
            }
            dstPos += stride;
        }
    }
}
