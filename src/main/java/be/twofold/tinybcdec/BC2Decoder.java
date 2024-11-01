package be.twofold.tinybcdec;

final class BC2Decoder extends BCDecoder {
    BC2Decoder() {
        super(BlockFormat.BC2);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decodeColorBlock(src, srcPos + 8, dst, dstPos, stride, true);
        decodeFixedAlpha(src, srcPos, dst, dstPos + 3, stride);
    }
}
