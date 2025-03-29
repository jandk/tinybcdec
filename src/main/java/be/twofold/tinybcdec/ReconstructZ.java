package be.twofold.tinybcdec;

final class ReconstructZ {
    static final byte[] NORMAL = new byte[256 * 256];

    static {
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                float r = x * (1.0f / 127.5f) - 1.0f;
                float g = y * (1.0f / 127.5f) - 1.0f;
                float sq = r * r + g * g;
                float nz = (float) Math.sqrt(1.0f - Math.min(1.0f, Math.max(sq, 0.0f)));
                NORMAL[(y << 8) + x] = (byte) (nz * 127.5f + 128.0f);
            }
        }
    }

    static void reconstruct(byte[] dst, int dstPos, int stride, int pixelStride) {
        for (int y = 0; y < BlockDecoder.BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BlockDecoder.BLOCK_WIDTH; x++) {
                int i = dstPos + x * pixelStride;
                int r = Byte.toUnsignedInt(dst[i/**/]);
                int g = Byte.toUnsignedInt(dst[i + 1]);
                dst[i + 2] = NORMAL[(g << 8) + r];
            }
            dstPos += stride;
        }
    }
}
