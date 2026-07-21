package be.twofold.tinybcdec;

import java.nio.*;

final class BC1Float extends BlockDecoder {
    static final int BPP = 16;

    private final float[] colors = new float[16];
    private final boolean bc2Or3;
    private final float alpha3;

    BC1Float(BC1Mode mode) {
        super(BPP, 8);
        this.bc2Or3 = mode == BC1Mode.BC2OR3;
        this.alpha3 = mode == BC1Mode.OPAQUE ? 1.0f : 0.0f;

        colors[+3] = 1.0f;
        colors[+7] = 1.0f;
        colors[11] = 1.0f;
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        long block = ByteIO.getLong(src, srcPos);

        int c0 = (int) (block/*   */) & 0xFFFF;
        int c1 = (int) (block >>> 16) & 0xFFFF;

        float r0 = ((c0 >>> 11) & 0x1F) * (1.0f / 31.0f);
        float g0 = ((c0 >>> +5) & 0x3F) * (1.0f / 63.0f);
        float b0 = ((c0/*   */) & 0x1F) * (1.0f / 31.0f);

        float r1 = ((c1 >>> 11) & 0x1F) * (1.0f / 31.0f);
        float g1 = ((c1 >>> +5) & 0x3F) * (1.0f / 63.0f);
        float b1 = ((c1/*   */) & 0x1F) * (1.0f / 31.0f);

        float[] colors = this.colors;
        colors[0] = r0;
        colors[1] = g0;
        colors[2] = b0;

        colors[4] = r1;
        colors[5] = g1;
        colors[6] = b1;

        if (c0 > c1 || bc2Or3) {
            colors[+8] = lerp(r0, r1, 1.0f / 3.0f);
            colors[+9] = lerp(g0, g1, 1.0f / 3.0f);
            colors[10] = lerp(b0, b1, 1.0f / 3.0f);

            colors[12] = lerp(r0, r1, 2.0f / 3.0f);
            colors[13] = lerp(g0, g1, 2.0f / 3.0f);
            colors[14] = lerp(b0, b1, 2.0f / 3.0f);
            colors[15] = 1.0f;
        } else {
            colors[+8] = lerp(r0, r1, 1.0f / 2.0f);
            colors[+9] = lerp(g0, g1, 1.0f / 2.0f);
            colors[10] = lerp(b0, b1, 1.0f / 2.0f);

            colors[12] = 0.0f;
            colors[13] = 0.0f;
            colors[14] = 0.0f;
            colors[15] = alpha3;
        }

        int indices = (int) (block >>> 32);
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int index = (indices & 0x03) * 4;
                int outPos = dstPos + x * BPP;
                ByteIO.setFloat(dst, outPos /**/, colors[index/**/]);
                ByteIO.setFloat(dst, outPos + +4, colors[index + 1]);
                ByteIO.setFloat(dst, outPos + +8, colors[index + 2]);
                ByteIO.setFloat(dst, outPos + 12, colors[index + 3]);
                indices >>>= 2;
            }
            dstPos += stride;
        }
    }
}
