package be.twofold.tinybcdec;

final class BC4UDecoder extends BlockDecoder {
    BC4UDecoder(int bytesPerPixel) {
        super(BlockFormat.BC4Unsigned, bytesPerPixel);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        int a0 = (int)  block        & 0xFF;
        int a1 = (int) (block >>> 8) & 0xFF;

        byte[] alphas = {(byte) a0, (byte) a1, 0, 0, 0, 0, 0, (byte) 0xFF};
        if (a0 > a1) {
            alphas[2] = scale7(6 * a0 +     a1);
            alphas[3] = scale7(5 * a0 + 2 * a1);
            alphas[4] = scale7(4 * a0 + 3 * a1);
            alphas[5] = scale7(3 * a0 + 4 * a1);
            alphas[6] = scale7(2 * a0 + 5 * a1);
            alphas[7] = scale7(    a0 + 6 * a1);
        } else {
            alphas[2] = scale5(4 * a0 +     a1);
            alphas[3] = scale5(3 * a0 + 2 * a1);
            alphas[4] = scale5(2 * a0 + 3 * a1);
            alphas[5] = scale5(    a0 + 4 * a1);
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

    private byte scale7(int i) {
        return (byte) (i * 9363 + 32768 >> 16);
    }

    private byte scale5(int i) {
        return (byte) (i * 13108 + 32768 >> 16);
    }
}
