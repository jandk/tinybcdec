package be.twofold.tinybcdec;

import java.nio.*;

final class BC2Float extends BlockDecoder {
    static final int BPP = 16;

    private final BC1Float colorDecoder = new BC1Float(BC1Mode.BC2OR3);

    BC2Float() {
        super(BPP, 16);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        decodeAlpha(src, srcPos, dst, dstPos + 12, stride);
    }

    private void decodeAlpha(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        long alphas = ByteIO.getLong(src, srcPos);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                float alpha = (alphas & 0x0F) * (1.0f / 15.0f);
                ByteIO.setFloat(dst, dstPos + x * BPP, alpha);
                alphas >>>= 4;
            }
            dstPos += stride;
        }
    }
}
