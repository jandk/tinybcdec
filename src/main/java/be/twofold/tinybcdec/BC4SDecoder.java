package be.twofold.tinybcdec;

final class BC4SDecoder extends BlockDecoder {
    BC4SDecoder() {
        super(BlockFormat.BC4Unsigned);
    }

    @Override
    @SuppressWarnings({"PointlessBitwiseExpression"})
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine) {
        long block = ByteArrays.getLong(src, srcPos);
        float a0 = unpack((byte) (block >>> 0));
        float a1 = unpack((byte) (block >>> 8));
        block >>>= 16;

        byte[] alphas = {pack(a0), pack(a1), 0, 0, 0, 0, 0, (byte) 255};
        if (a0 > a1) {
            alphas[2] = pack(lerp(a0, a1, 1.0f / 7.0f));
            alphas[3] = pack(lerp(a0, a1, 2.0f / 7.0f));
            alphas[4] = pack(lerp(a0, a1, 3.0f / 7.0f));
            alphas[5] = pack(lerp(a0, a1, 4.0f / 7.0f));
            alphas[6] = pack(lerp(a0, a1, 5.0f / 7.0f));
            alphas[7] = pack(lerp(a0, a1, 6.0f / 7.0f));
        } else {
            alphas[2] = pack(lerp(a0, a1, 1.0f / 5.0f));
            alphas[3] = pack(lerp(a0, a1, 2.0f / 5.0f));
            alphas[4] = pack(lerp(a0, a1, 3.0f / 5.0f));
            alphas[5] = pack(lerp(a0, a1, 4.0f / 5.0f));
        }

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                dst[dstPos] = alphas[(int) (block & 7)];
                block >>>= 3;
                dstPos += BYTES_PER_PIXEL;
            }
            dstPos += bytesPerLine - BLOCK_WIDTH * BYTES_PER_PIXEL;
        }
    }

    private static byte pack(float f) {
        return (byte) Math.fma(f, 127.5f, 128.0f);
    }

    private static float unpack(byte b) {
        return Math.max(b, -127) / 127.0f;
    }

    private static float lerp(float a, float b, float t) {
        return Math.fma(t, b - a, a);
    }
}
