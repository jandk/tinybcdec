package be.twofold.tinybcdec;

final class BC1Decoder extends BCDecoder {
    private final boolean opaque;

    BC1Decoder(BCOrder order, boolean opaque) {
        super(BCFormat.BC1, order);
        if (bytesPerPixel < 4) {
            throw new IllegalArgumentException("bytesPerPixel must be at least 4");
        }
        this.opaque = opaque;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        // After a lot of benchmarking, it appears that just doing the full float vs fixed point
        // doesn't really matter. The fixed point version is slightly faster, but the difference is
        // so small that it's not worth it. The fixed point version is also harder to read and
        // understand, so I'm going with the float version.
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        int c0 = (int) (block       ) & 0xffff;
        int c1 = (int) (block >>> 16) & 0xffff;

        float r0 = ((c0 >>> 11) & 0x1f) * (1.0f / 31.0f);
        float g0 = ((c0 >>>  5) & 0x3f) * (1.0f / 63.0f);
        float b0 = ( c0         & 0x1f) * (1.0f / 31.0f);

        float r1 = ((c1 >>> 11) & 0x1f) * (1.0f / 31.0f);
        float g1 = ((c1 >>> 5)  & 0x3f) * (1.0f / 63.0f);
        float b1 = ( c1         & 0x1f) * (1.0f / 31.0f);
        // @formatter:on

        int[] colors = {rgb(r0, g0, b0), rgb(r1, g1, b1), 0, 0};
        if (c0 > c1 || opaque) {
            float r2 = lerp(r0, r1, 1.0f / 3.0f);
            float g2 = lerp(g0, g1, 1.0f / 3.0f);
            float b2 = lerp(b0, b1, 1.0f / 3.0f);
            colors[2] = rgb(r2, g2, b2);

            float r3 = lerp(r0, r1, 2.0f / 3.0f);
            float g3 = lerp(g0, g1, 2.0f / 3.0f);
            float b3 = lerp(b0, b1, 2.0f / 3.0f);
            colors[3] = rgb(r3, g3, b3);
        } else {
            float r2 = lerp(r0, r1, 1.0f / 2.0f);
            float g2 = lerp(g0, g1, 1.0f / 2.0f);
            float b2 = lerp(b0, b1, 1.0f / 2.0f);
            colors[2] = rgb(r2, g2, b2);
        }

        int indices = (int) (block >>> 32);
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                ByteArrays.setInt(dst, dstPos, colors[indices & 3]);
                indices >>>= 2;
                dstPos += bytesPerPixel;
            }
            dstPos += stride - BLOCK_WIDTH * bytesPerPixel;
        }
    }

    private int rgb(float r, float g, float b) {
        return rgba(pack(r), pack(g), pack(b), 255);
    }

    private static int pack(float value) {
        return (int) Math.fma(value, 255.0f, 0.5f);
    }

    private static float lerp(float a, float b, float t) {
        return Math.fma(t, b - a, a);
    }
}
