package be.twofold.tinybcdec;

final class BCDecoder {
    static final int BLOCK_WIDTH = 4;
    static final int BLOCK_HEIGHT = 4;
    static final int BYTES_PER_PIXEL = 4;

    private BCDecoder() {
    }

    static void decodeColorBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride, boolean opaque) {
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

        int[] colors = {
            rgb(r0, g0, b0),
            rgb(r1, g1, b1),
            0,
            0
        };
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
                ByteArrays.setInt(dst, dstPos + x * BYTES_PER_PIXEL, colors[index]);
                indices >>>= 2;
            }
            dstPos += stride;
        }
    }

    static void decodeFixedAlpha(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long alphas = ByteArrays.getLong(src, srcPos);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                byte value = (byte) ((alphas & 0x0F) * 0x11);
                dst[dstPos + x * BYTES_PER_PIXEL] = value;
                alphas >>>= 4;
            }
            dstPos += stride;
        }
    }

    static void decodeAlphaUnsigned(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        int a0 = (int) ( block        & 0xFF);
        int a1 = (int) ((block >>> 8) & 0xFF);
        block >>>= 16;

        byte[] alphas = {(byte) a0, (byte) a1, 0, 0, 0, 0, 0, (byte) 0xFF};
        if (a0 > a1) {
            alphas[2] = (byte) ((6 * a0 +     a1 + 3) / 7);
            alphas[3] = (byte) ((5 * a0 + 2 * a1 + 3) / 7);
            alphas[4] = (byte) ((4 * a0 + 3 * a1 + 3) / 7);
            alphas[5] = (byte) ((3 * a0 + 4 * a1 + 3) / 7);
            alphas[6] = (byte) ((2 * a0 + 5 * a1 + 3) / 7);
            alphas[7] = (byte) ((    a0 + 6 * a1 + 3) / 7);
        } else {
            alphas[2] = (byte) ((4 * a0 +     a1 + 2) / 5);
            alphas[3] = (byte) ((3 * a0 + 2 * a1 + 2) / 5);
            alphas[4] = (byte) ((2 * a0 + 3 * a1 + 2) / 5);
            alphas[5] = (byte) ((    a0 + 4 * a1 + 2) / 5);
        }
        // @formatter:on

        storeAlphas(dst, dstPos, stride, block, alphas);
    }

    static void decodeAlphaSigned(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        float a0 = unpackSNorm((byte)  block       );
        float a1 = unpackSNorm((byte) (block >>> 8));
        block >>>= 16;
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

        storeAlphas(dst, dstPos, stride, block, alphas);
    }

    static void expandGray(byte[] dst, int dstPos, int stride) {
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int i = dstPos + x * BYTES_PER_PIXEL;
                int v = Byte.toUnsignedInt(dst[i]);
                ByteArrays.setInt(dst, i, BlockDecoder.rgba(v, v, v, 0xFF));
            }
            dstPos += stride;
        }
    }

    static void fillAlpha(byte[] dst, int dstPos, int stride) {
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                dst[dstPos + x * BYTES_PER_PIXEL + 3] = (byte) 0xFF;
            }
            dstPos += stride;
        }
    }

    private static void storeAlphas(byte[] dst, int dstPos, int stride, long indices, byte[] alphas) {
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int index = (int) (indices & 0x07);
                dst[dstPos + x * BYTES_PER_PIXEL] = alphas[index];
                indices >>>= 3;
            }
            dstPos += stride;
        }
    }

    private static int rgb(float r, float g, float b) {
        return BlockDecoder.rgba(packUNorm(r), packUNorm(g), packUNorm(b), 0xFF);
    }

    private static int packUNorm(float value) {
        return (int) Math.fma(value, 255.0f, 0.5f);
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
