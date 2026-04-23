package be.twofold.tinybcdec;

import java.nio.*;

final class BC5S extends BlockDecoder {
    private static final int BPP = 2;
    private static final BC4S DECODER = new BC4S(BPP);

    static final BlockDecoder INSTANCE = new BC5S();

    private BC5S() {
        super(BPP, 16);
    }

    @Override
    public void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        DECODER.decodeBlock(src, srcPos/**/, dst, dstPos/**/, stride);
        DECODER.decodeBlock(src, srcPos + 8, dst, dstPos + 1, stride);
    }
}
