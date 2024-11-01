package be.twofold.tinybcdec;

final class BC4UDecoder extends BCDecoder {
    BC4UDecoder() {
        super(BlockFormat.BC4Unsigned);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decodeAlphaUnsigned(src, srcPos, dst, dstPos, stride);
        expandGray(dst, dstPos, stride);
    }
}
