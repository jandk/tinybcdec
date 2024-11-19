package be.twofold.tinybcdec;

final class BC4UDecoder extends BlockDecoder {
    BC4UDecoder(int bytesPerPixel) {
        super(BlockFormat.BC4Unsigned, bytesPerPixel);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        int a0 = (int) (block & 0xFF);
        int a1 = (int) ((block >>> 8) & 0xFF);

        byte[] alphas = {(byte) a0, (byte) a1, 0, 0, 0, 0, 0, (byte) 0xFF};
        if (a0 > a1) {
            alphas[2] = (byte) scale7(6 * a0 + 1 * a1);
            alphas[3] = (byte) scale7(5 * a0 + 2 * a1);
            alphas[4] = (byte) scale7(4 * a0 + 3 * a1);
            alphas[5] = (byte) scale7(3 * a0 + 4 * a1);
            alphas[6] = (byte) scale7(2 * a0 + 5 * a1);
            alphas[7] = (byte) scale7(1 * a0 + 6 * a1);
        } else {
            alphas[2] = (byte) scale5(4 * a0 + 1 * a1);
            alphas[3] = (byte) scale5(3 * a0 + 2 * a1);
            alphas[4] = (byte) scale5(2 * a0 + 3 * a1);
            alphas[5] = (byte) scale5(1 * a0 + 4 * a1);
        }

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

    private int scale7(int i) {
        return i * 9363 + 32768 >> 16;
    }

    private int scale5(int i) {
        return i * 13108 + 32768 >> 16;
    }
}
