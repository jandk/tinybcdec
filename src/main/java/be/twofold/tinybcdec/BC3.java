package be.twofold.tinybcdec;

import java.nio.*;

final class BC3 extends BlockDecoder {
    private static final int BPP = 4;

    private final BC1 colorDecoder = new BC1(BC1Mode.BC2OR3);
    private final BC4U alphaDecoder = new BC4U(BPP);

    BC3() {
        super(BPP, 16);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos/**/, stride);
        alphaDecoder.decodeBlock(src, srcPos/**/, dst, dstPos + 3, stride);
    }
}
