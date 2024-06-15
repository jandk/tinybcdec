package be.twofold.tinybcdec;

final class BC5UDecoder extends BCDecoder {
    private static final byte[] NORMAL = new byte[256 * 256];
    private final BC4UDecoder rDecoder;
    private final BC4UDecoder gDecoder;
    private final boolean normalize;

    static {
        for (var y = 0; y < 256; y++) {
            for (var x = 0; x < 256; x++) {
                var xx = Math.fma(x, (1.0f / 127.5f), -1.0f);
                var yy = Math.fma(y, (1.0f / 127.5f), -1.0f);
                var sq = xx * xx + yy * yy;
                var nz = (float) Math.sqrt(1.0f - Math.min(1.0f, Math.max(sq, 0.0f)));
                NORMAL[y * 256 + x] = (byte) Math.fma(nz, 127.5f, 128.0f);
            }
        }
    }

    BC5UDecoder(int bytesPerPixel, int rOffset, int gOffset) {
        super(16, bytesPerPixel, rOffset, gOffset, -1, -1);
        this.rDecoder = new BC4UDecoder(bytesPerPixel, rOffset);
        this.gDecoder = new BC4UDecoder(bytesPerPixel, gOffset);
        this.normalize = false;
    }

    BC5UDecoder(int bytesPerPixel, int rOffset, int gOffset, int bOffset) {
        super(16, bytesPerPixel, rOffset, gOffset, bOffset, -1);
        this.rDecoder = new BC4UDecoder(bytesPerPixel, rOffset);
        this.gDecoder = new BC4UDecoder(bytesPerPixel, gOffset);
        this.normalize = true;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        rDecoder.decodeBlock(src, srcPos, dst, dstPos, stride);
        gDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);

        if (normalize) {
            for (int y = 0; y < BLOCK_HEIGHT; y++) {
                for (int x = 0; x < BLOCK_WIDTH; x++) {
                    int r = Byte.toUnsignedInt(dst[dstPos + rOffset]);
                    int g = Byte.toUnsignedInt(dst[dstPos + gOffset]);
                    dst[dstPos + bOffset] = NORMAL[g * 256 + r];
                    dstPos += bytesPerPixel;
                }
                dstPos += stride - BLOCK_WIDTH * bytesPerPixel;
            }
        }
    }
}
