package be.twofold.tinybcdec;

import java.nio.*;

final class BC4UFloat extends BlockDecoder {
    static final int BPP = 16;

    private final float[] alphas = new float[8];

    BC4UFloat() {
        super(BPP, 8);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        float[] alphas = this.alphas;
        long indices = buildAlphas(ByteIO.getLong(src, srcPos), alphas);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int pos = dstPos + x * BPP;
                float alpha = alphas[(int) (indices & 0x07)];
                ByteIO.setFloat(dst, pos /**/, alpha);
                ByteIO.setFloat(dst, pos + +4, alpha);
                ByteIO.setFloat(dst, pos + +8, alpha);
                ByteIO.setFloat(dst, pos + 12, +1.0f);
                indices >>>= 3;
            }
            dstPos += stride;
        }
    }

    static long buildAlphas(long block, float[] alphas) {
        float a0 = ((block/*  */) & 0xFF) * (1.0f / 255.0f);
        float a1 = ((block >>> 8) & 0xFF) * (1.0f / 255.0f);

        alphas[0] = a0;
        alphas[1] = a1;
        if (a0 > a1) {
            alphas[2] = lerp(a0, a1, 1.0f / 7.0f);
            alphas[3] = lerp(a0, a1, 2.0f / 7.0f);
            alphas[4] = lerp(a0, a1, 3.0f / 7.0f);
            alphas[5] = lerp(a0, a1, 4.0f / 7.0f);
            alphas[6] = lerp(a0, a1, 5.0f / 7.0f);
            alphas[7] = lerp(a0, a1, 6.0f / 7.0f);
        } else {
            alphas[2] = lerp(a0, a1, 1.0f / 5.0f);
            alphas[3] = lerp(a0, a1, 2.0f / 5.0f);
            alphas[4] = lerp(a0, a1, 3.0f / 5.0f);
            alphas[5] = lerp(a0, a1, 4.0f / 5.0f);
            alphas[6] = 0.0f;
            alphas[7] = 1.0f;
        }

        return block >>> 16;
    }
}
