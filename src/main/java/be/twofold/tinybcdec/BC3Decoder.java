package be.twofold.tinybcdec;

final class BC3Decoder extends BCDecoder {
    BC3Decoder() {
        super(BlockFormat.BC3);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decodeColorBlock(src, srcPos + 8, dst, dstPos, stride, true);
        decodeAlphaUnsigned(src, srcPos, dst, dstPos + 3, stride);
    }
}
