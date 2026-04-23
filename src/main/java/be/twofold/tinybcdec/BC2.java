package be.twofold.tinybcdec;

import java.nio.*;

final class BC2 extends BlockDecoder {
    private static final int BPP = 4;
    private static final BC1 COLOR_DECODER = new BC1(BC1Mode.BC2OR3);

    static final BlockDecoder INSTANCE = new BC2();

    private BC2() {
        super(BPP, 16);
    }

    @Override
    public void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        COLOR_DECODER.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        decodeAlpha(src, srcPos, dst, dstPos + 3, stride);
    }

    private void decodeAlpha(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        long alphas = ByteIO.getLong(src, srcPos);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                byte alpha = (byte) ((alphas & 15) * 17);
                ByteIO.setByte(dst, dstPos + x * BPP, alpha);
                alphas >>>= 4;
            }
            dstPos += stride;
        }
    }
}
