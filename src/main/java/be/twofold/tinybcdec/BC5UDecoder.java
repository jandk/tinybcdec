package be.twofold.tinybcdec;

final class BC5UDecoder extends BCDecoder {
    private static final byte[] NORMAL = new byte[256 * 256];
    private final BC4UDecoder rDecoder;
    private final BC4UDecoder gDecoder;
    private final boolean normalized;

    static {
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                float xx = Math.fma(x, (1.0f / 127.5f), -1.0f);
                float yy = Math.fma(y, (1.0f / 127.5f), -1.0f);
                float sq = xx * xx + yy * yy;
                float nz = (float) Math.sqrt(1.0f - Math.min(1.0f, Math.max(sq, 0.0f)));
                NORMAL[y * 256 + x] = (byte) Math.fma(nz, 127.5f, 128.0f);
            }
        }
    }

    BC5UDecoder(BCFormat format, BCOrder order) {
        super(format, order);
        this.rDecoder = new BC4UDecoder(BCOrder.single(order.count(), redOffset));
        this.gDecoder = new BC4UDecoder(BCOrder.single(order.count(), greenOffset));
        this.normalized = format == BCFormat.BC5UnsignedNormalized;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        rDecoder.decodeBlock(src, srcPos, dst, dstPos, stride);
        gDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);

        if (normalized) {
            for (int y = 0; y < BLOCK_HEIGHT; y++) {
                for (int x = 0; x < BLOCK_WIDTH; x++) {
                    int r = Byte.toUnsignedInt(dst[dstPos + redOffset]);
                    int g = Byte.toUnsignedInt(dst[dstPos + greenOffset]);
                    dst[dstPos + blueOffset] = NORMAL[g * 256 + r];
                    dstPos += bytesPerPixel;
                }
                dstPos += stride - BLOCK_WIDTH * bytesPerPixel;
            }
        }
    }
}
