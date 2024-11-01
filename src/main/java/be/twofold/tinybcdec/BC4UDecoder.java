package be.twofold.tinybcdec;

final class BC4UDecoder extends BlockDecoder {
    BC4UDecoder() {
        super(BlockFormat.BC4Unsigned);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        BCDecoder.decodeAlphaUnsigned(src, srcPos, dst, dstPos, stride);
        BCDecoder.expandGray(dst, dstPos, stride);
    }
}
