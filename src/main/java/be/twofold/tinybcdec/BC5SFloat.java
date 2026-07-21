package be.twofold.tinybcdec;

import java.nio.*;

final class BC5SFloat extends BlockDecoder {
    static final int BPP = 16;

    private final float[] rAlphas = new float[8];
    private final float[] gAlphas = new float[8];

    BC5SFloat() {
        super(BPP, 16);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        float[] rAlphas = this.rAlphas;
        float[] gAlphas = this.gAlphas;

        long rIndices = BC4SFloat.buildAlphas(ByteIO.getLong(src, srcPos/**/), rAlphas);
        long gIndices = BC4SFloat.buildAlphas(ByteIO.getLong(src, srcPos + 8), gAlphas);
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int offset = dstPos + x * BPP;
                ByteIO.setFloat(dst, offset /**/, rAlphas[(int) (rIndices & 0x07)]);
                ByteIO.setFloat(dst, offset + +4, gAlphas[(int) (gIndices & 0x07)]);
                ByteIO.setFloat(dst, offset + +8, 0.0f);
                ByteIO.setFloat(dst, offset + 12, 1.0f);
                rIndices >>>= 3;
                gIndices >>>= 3;
            }
            dstPos += stride;
        }
    }
}
