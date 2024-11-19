package be.twofold.tinybcdec;

final class BC1Decoder extends BlockDecoder {
    private static final int BPP = 4;

    private final boolean opaque;

    BC1Decoder(boolean opaque) {
        super(BlockFormat.BC1, BPP);
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
        int c0 = (int) (block       ) & 0xFFFF;
        int c1 = (int) (block >>> 16) & 0xFFFF;

        float r0 = ((c0 >>> 11) & 0x1F) * (1.0f / 31.0f);
        float g0 = ((c0 >>>  5) & 0x3F) * (1.0f / 63.0f);
        float b0 = ( c0         & 0x1F) * (1.0f / 31.0f);

        float r1 = ((c1 >>> 11) & 0x1F) * (1.0f / 31.0f);
        float g1 = ((c1 >>> 5)  & 0x3F) * (1.0f / 63.0f);
        float b1 = ( c1         & 0x1F) * (1.0f / 31.0f);
        // @formatter:on

        int[] colors = {rgb(r0, g0, b0), rgb(r1, g1, b1), 0, 0};
        if (c0 > c1 || opaque) {
            float r2 = lerp(r0, r1, 1.0f / 3.0f);
            float g2 = lerp(g0, g1, 1.0f / 3.0f);
            float b2 = lerp(b0, b1, 1.0f / 3.0f);
            colors[2] = rgb(r2, g2, b2);

            float r3 = lerp(r1, r0, 1.0f / 3.0f);
            float g3 = lerp(g1, g0, 1.0f / 3.0f);
            float b3 = lerp(b1, b0, 1.0f / 3.0f);
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
                int index = indices & 3;
                ByteArrays.setInt(dst, dstPos + x * BPP, colors[index]);
                indices >>>= 2;
            }
            dstPos += stride;
        }
    }

    private static int rgb(float r, float g, float b) {
        return pack(r) | pack(g) << 8 | pack(b) << 16 | 0xFF000000;
    }

    private static int pack(float value) {
        return (int) Math.fma(value, 255.0f, 0.5f);
    }

    private static float lerp(float a, float b, float t) {
        return Math.fma(t, b - a, a);
    }
}
