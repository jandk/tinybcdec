package be.twofold.tinybcdec;

final class ReconstructZ {
    private static final byte[] NORMAL = new byte[256 * 256];

    private ReconstructZ() {
    }

    static {
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                float r = x * (1.0f / 127.5f) - 1.0f;
                float g = y * (1.0f / 127.5f) - 1.0f;
                float b = (float) Math.sqrt(1.0f - Math.min(1.0f, Math.max(r * r + g * g, 0.0f)));
                NORMAL[(y << 8) + x] = (byte) (b * 127.5f + 128.0f);
            }
        }
    }

    static void reconstruct(byte[] dst, int dstPos, int lineStride, int pixelStride) {
        for (int y = 0; y < BlockDecoder.BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BlockDecoder.BLOCK_WIDTH; x++) {
                int i = dstPos + x * pixelStride;
                int r = Byte.toUnsignedInt(dst[i/**/]);
                int g = Byte.toUnsignedInt(dst[i + 1]);
                dst[i + 2] = NORMAL[(g << 8) + r];
            }
            dstPos += lineStride;
        }
    }
}
