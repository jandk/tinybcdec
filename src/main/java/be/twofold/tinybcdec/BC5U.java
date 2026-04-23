package be.twofold.tinybcdec;

import java.nio.*;

final class BC5U extends BlockDecoder {
    private static final int BPP = 2;

    private final BC4U decoder = new BC4U(BPP);

    BC5U() {
        super(BPP, 16);
    }

    @Override
    public void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        decoder.decodeBlock(src, srcPos/**/, dst, dstPos/**/, stride);
        decoder.decodeBlock(src, srcPos + 8, dst, dstPos + 1, stride);
    }
}
