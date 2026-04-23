package be.twofold.tinybcdec;

import java.nio.*;

final class BC5U extends BlockDecoder {
    private static final int BPP = 2;
    private static final BC4U DECODER = new BC4U(BPP);

    static final BlockDecoder INSTANCE = new BC5U();

    private BC5U() {
        super(BPP, 16);
    }

    @Override
    public void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        DECODER.decodeBlock(src, srcPos/**/, dst, dstPos/**/, stride);
        DECODER.decodeBlock(src, srcPos + 8, dst, dstPos + 1, stride);
    }
}
