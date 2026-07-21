package be.twofold.tinybcdec;

import java.nio.*;

final class BC3 extends BlockDecoder {
    static final int BPP = 4;

    private final BC1 colorDecoder = new BC1(BC1Mode.BC2OR3);
    private final byte[] alphas = new byte[8];

    BC3() {
        super(BPP, 16);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos/**/, stride);
        decodeAlpha(src, srcPos/**/, dst, dstPos + 3, stride);
    }

    private void decodeAlpha(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        byte[] alphas = this.alphas;
        long indices = BC4U.buildAlphas(ByteIO.getLong(src, srcPos), alphas);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                ByteIO.setByte(dst, dstPos + x * BPP, alphas[(int) (indices & 0x07)]);
                indices >>>= 3;
            }
            dstPos += stride;
        }
    }
}
