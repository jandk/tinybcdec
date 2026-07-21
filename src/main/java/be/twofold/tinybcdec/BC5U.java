package be.twofold.tinybcdec;

import java.nio.*;

final class BC5U extends BlockDecoder {
    static final int BPP = 4;

    private final byte[] rAlphas = new byte[8];
    private final byte[] gAlphas = new byte[8];

    BC5U() {
        super(BPP, 16);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        byte[] rAlphas = this.rAlphas;
        byte[] gAlphas = this.gAlphas;

        long rIndices = BC4U.buildAlphas(src.getLong(srcPos/**/), rAlphas);
        long gIndices = BC4U.buildAlphas(src.getLong(srcPos + 8), gAlphas);
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int rAlpha = rAlphas[(int) (rIndices & 0x07)] & 0xFF;
                int gAlpha = gAlphas[(int) (gIndices & 0x07)] & 0xFF;
                ByteIO.setInt(dst, dstPos + x * BPP, gAlpha << 8 | rAlpha << 16 | 0xFF00_0000);
                rIndices >>>= 3;
                gIndices >>>= 3;
            }
            dstPos += stride;
        }
    }
}
