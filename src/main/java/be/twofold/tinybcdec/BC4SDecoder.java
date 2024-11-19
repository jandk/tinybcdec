package be.twofold.tinybcdec;

final class BC4SDecoder extends BlockDecoder {
    BC4SDecoder(int bytesPerPixel) {
        super(BlockFormat.BC4Signed, bytesPerPixel);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        float a0 = unpackSNorm((byte)  block       );
        float a1 = unpackSNorm((byte) (block >>> 8));
        // @formatter:on

        byte[] alphas = {packSNormToUNorm(a0), packSNormToUNorm(a1), 0, 0, 0, 0, 0, (byte) 0xFF};
        if (a0 > a1) {
            alphas[2] = packSNormToUNorm(lerp(a0, a1, 1.0f / 7.0f));
            alphas[3] = packSNormToUNorm(lerp(a0, a1, 2.0f / 7.0f));
            alphas[4] = packSNormToUNorm(lerp(a0, a1, 3.0f / 7.0f));
            alphas[5] = packSNormToUNorm(lerp(a0, a1, 4.0f / 7.0f));
            alphas[6] = packSNormToUNorm(lerp(a0, a1, 5.0f / 7.0f));
            alphas[7] = packSNormToUNorm(lerp(a0, a1, 6.0f / 7.0f));
        } else {
            alphas[2] = packSNormToUNorm(lerp(a0, a1, 1.0f / 5.0f));
            alphas[3] = packSNormToUNorm(lerp(a0, a1, 2.0f / 5.0f));
            alphas[4] = packSNormToUNorm(lerp(a0, a1, 3.0f / 5.0f));
            alphas[5] = packSNormToUNorm(lerp(a0, a1, 4.0f / 5.0f));
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

    private static byte packSNormToUNorm(float f) {
        return (byte) Math.fma(f, 127.5f, 128.0f);
    }

    private static float unpackSNorm(byte b) {
        return Math.max(b, -127) * (1.0f / 127.0f);
    }

    private static float lerp(float a, float b, float t) {
        return Math.fma(t, b - a, a);
    }
}
