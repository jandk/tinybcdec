package be.twofold.tinybcdec;

import java.nio.*;

final class BC2 extends BlockDecoder {
    static final int BPP = 4;

    private final BC1 colorDecoder = new BC1(BC1Mode.BC2OR3);

    BC2() {
        super(BPP, 16);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        decodeAlpha(src, srcPos, dst, dstPos + 3, stride);
    }

    private void decodeAlpha(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        long alphas = ByteIO.getLong(src, srcPos);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                byte alpha = (byte) ((alphas & 0x0F) * 0x11);
                ByteIO.setByte(dst, dstPos + x * BPP, alpha);
                alphas >>>= 4;
            }
            dstPos += stride;
        }
    }
}
