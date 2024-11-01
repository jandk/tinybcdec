package be.twofold.tinybcdec;

final class ReconstructZ {
    static final byte[] NORMAL = new byte[256 * 256];

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

    static void reconstruct(byte[] dst, int dstPos, int stride) {
        for (int y = 0; y < BCDecoder.BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BCDecoder.BLOCK_WIDTH; x++) {
                int i = dstPos + x * BCDecoder.BYTES_PER_PIXEL;
                int r = Byte.toUnsignedInt(dst[i]);
                int g = Byte.toUnsignedInt(dst[i + 1]);
                dst[i + 2] = NORMAL[g * 256 + r];
            }
            dstPos += stride;
        }
    }
}
