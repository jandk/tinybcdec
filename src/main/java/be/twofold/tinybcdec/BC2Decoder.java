package be.twofold.tinybcdec;

final class BC2Decoder extends BlockDecoder {
    BC2Decoder() {
        super(BlockFormat.BC2);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        BCDecoder.decodeColorBlock(src, srcPos + 8, dst, dstPos, stride, true);
        BCDecoder.decodeFixedAlpha(src, srcPos, dst, dstPos + 3, stride);
    }
}
