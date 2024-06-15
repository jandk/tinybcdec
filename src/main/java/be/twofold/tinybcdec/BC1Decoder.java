package be.twofold.tinybcdec;

final class BC1Decoder extends BlockDecoder {
    private final boolean opaque;

    public BC1Decoder(int bytesPerPixel, int rOffset, int gOffset, int bOffset, int aOffset, boolean opaque) {
        super(8, bytesPerPixel, rOffset, gOffset, bOffset, aOffset);
        if (bytesPerPixel < 4) {
            throw new IllegalArgumentException("bytesPerPixel must be at least 4");
        }
        this.opaque = opaque;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);
        int c0 = (int) (block & 0xffff);
        int c1 = (int) ((block >>> 16) & 0xffff);
        int indices = (int) (block >>> 32);

        int r0 = expand5to8((c0 >>> 11) & 0x1f);
        int g0 = expand6to8((c0 >>> 5) & 0x3f);
        int b0 = expand5to8((c0) & 0x1f);

        int r1 = expand5to8((c1 >>> 11) & 0x1f);
        int g1 = expand6to8((c1 >>> 5) & 0x3f);
        int b1 = expand5to8((c1) & 0x1f);

        int[] colors = new int[4];
        colors[0] = rgb(r0, g0, b0);
        colors[1] = rgb(r1, g1, b1);

        if (c0 > c1 || opaque) {
            int r2 = ((r0 << 1) + r1 + 1) / 3;
            int g2 = ((g0 << 1) + g1 + 1) / 3;
            int b2 = ((b0 << 1) + b1 + 1) / 3;
            colors[2] = rgb(r2, g2, b2);

            int r3 = (r0 + (r1 << 1) + 1) / 3;
            int g3 = (g0 + (g1 << 1) + 1) / 3;
            int b3 = (b0 + (b1 << 1) + 1) / 3;
            colors[3] = rgb(r3, g3, b3);
        } else {
            int r2 = (r0 + r1 + 1) >>> 1;
            int g2 = (g0 + g1 + 1) >>> 1;
            int b2 = (b0 + b1 + 1) >>> 1;
            colors[2] = rgb(r2, g2, b2);
        }

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                ByteArrays.setInt(dst, dstPos, colors[indices & 3]);
                indices >>>= 2;
                dstPos += bytesPerPixel;
            }
            dstPos += stride - 4 * bytesPerPixel;
        }
    }

    private static int expand5to8(int value) {
        return (value * 527 + 23) >>> 6;
    }

    private static int expand6to8(int value) {
        return (value * 259 + 33) >>> 6;
    }
}
