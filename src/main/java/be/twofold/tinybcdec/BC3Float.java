package be.twofold.tinybcdec;

import java.nio.*;

final class BC3Float extends BlockDecoder {
    static final int BPP = 16;

    private final BC1Float colorDecoder = new BC1Float(BC1Mode.BC2OR3);
    private final float[] alphas = new float[8];

    BC3Float() {
        super(BPP, 16);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        decodeAlpha(src, srcPos, dst, dstPos + 12, stride);
    }

    private void decodeAlpha(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        float[] alphas = this.alphas;
        long indices = BC4UFloat.buildAlphas(ByteIO.getLong(src, srcPos), alphas);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                float alpha = alphas[(int) (indices & 0x07)];
                ByteIO.setFloat(dst, dstPos + x * BPP, alpha);
                indices >>>= 3;
            }
            dstPos += stride;
        }
    }
}
