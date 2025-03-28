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
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        int c0 = (int)  block         & 0xFFFF;
        int c1 = (int) (block >>> 16) & 0xFFFF;

        int r0 = (c0 >>> 11) & 0x1F;
        int g0 = (c0 >>>  5) & 0x3F;
        int b0 =  c0         & 0x1F;

        int r1 = (c1 >>> 11) & 0x1F;
        int g1 = (c1 >>>  5) & 0x3F;
        int b1 =  c1         & 0x1F;
        // @formatter:on

        int[] colors = {
            rgb(scale031(r0), scale063(g0), scale031(b0)),
            rgb(scale031(r1), scale063(g1), scale031(b1)),
            0, 0
        };
        if (c0 > c1 || opaque) {
            int r2 = scale093(2 * r0 + r1);
            int g2 = scale189(2 * g0 + g1);
            int b2 = scale093(2 * b0 + b1);
            colors[2] = rgb(r2, g2, b2);

            int r3 = scale093(r0 + 2 * r1);
            int g3 = scale189(g0 + 2 * g1);
            int b3 = scale093(b0 + 2 * b1);
            colors[3] = rgb(r3, g3, b3);
        } else {
            int r2 = scale062(r0 + r1);
            int g2 = scale126(g0 + g1);
            int b2 = scale062(b0 + b1);
            colors[2] = rgb(r2, g2, b2);
        }

        int indices = (int) (block >>> 32);
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int color = colors[indices & 0x03];
                ByteArrays.setInt(dst, dstPos + x * BPP, color);
                indices >>>= 2;
            }
            dstPos += stride;
        }
    }

    private static int rgb(int r, int g, int b) {
        return r | g << 8 | b << 16 | 0xFF000000;
    }

    private static int scale031(int i) {
        return (i * 527 + 23) >>> 6;
    }

    private static int scale063(int i) {
        return (i * 259 + 33) >>> 6;
    }

    private static int scale093(int i) {
        return (i * 351 + 61) >>> 7;
    }

    private static int scale189(int i) {
        return (i * 2763 + 1039) >>> 11;
    }

    private static int scale062(int i) {
        return (i * 1053 + 125) >>> 8;
    }

    private static int scale126(int i) {
        return (i * 4145 + 1019) >>> 11;
    }
}
