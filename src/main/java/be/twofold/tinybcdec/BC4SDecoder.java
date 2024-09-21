package be.twofold.tinybcdec;

final class BC4SDecoder extends BlockDecoder {
    BC4SDecoder(PixelOrder order) {
        super(BlockFormat.BC4Unsigned, order);
    }

    @Override
    @SuppressWarnings({"PointlessBitwiseExpression"})
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine) {
        long block = ByteArrays.getLong(src, srcPos);
        byte a0 = (byte) (block >>> 0);
        byte a1 = (byte) (block >>> 8);
        block >>>= 16;

        float f0 = unpack(a0);
        float f1 = unpack(a1);
        byte[] alphas = {pack(f0), pack(f1), 0, 0, 0, 0, 0, (byte) 255};
        if (a0 > a1) {
            alphas[2] = pack((f0 * 6f + f1 * 1f) / 7f);
            alphas[3] = pack((f0 * 5f + f1 * 2f) / 7f);
            alphas[4] = pack((f0 * 4f + f1 * 3f) / 7f);
            alphas[5] = pack((f0 * 3f + f1 * 4f) / 7f);
            alphas[6] = pack((f0 * 2f + f1 * 5f) / 7f);
            alphas[7] = pack((f0 * 1f + f1 * 6f) / 7f);
        } else {
            alphas[2] = pack((f0 * 4f + f1 * 1f) / 5f);
            alphas[3] = pack((f0 * 3f + f1 * 2f) / 5f);
            alphas[4] = pack((f0 * 2f + f1 * 3f) / 5f);
            alphas[5] = pack((f0 * 1f + f1 * 4f) / 5f);
        }

        dstPos += redOffset;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                dst[dstPos] = alphas[(int) (block & 7)];
                block >>>= 3;
                dstPos += bytesPerPixel;
            }
            dstPos += bytesPerLine - BLOCK_WIDTH * bytesPerPixel;
        }
    }

    private byte pack(float f) {
        return (byte) ((f * 0.5f + 0.5f) * 255f + 0.5f);
    }

    private float unpack(byte b) {
        return Math.max(b, -127) / 127f;
    }

    private float lerp(float a, float b, float t) {
        return Math.fma(t, b - a, a);
    }
}
