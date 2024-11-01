package be.twofold.tinybcdec;

final class BC5UDecoder extends BCDecoder {
    private final boolean normalized;

    BC5UDecoder(BlockFormat format) {
        super(format);
        this.normalized = format == BlockFormat.BC5UnsignedNormalized;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decodeAlphaUnsigned(src, srcPos, dst, dstPos, stride);
        decodeAlphaUnsigned(src, srcPos + 8, dst, dstPos + 1, stride);
        fillAlpha(dst, dstPos, stride);

        if (normalized) {
            ReconstructZ.reconstruct(dst, dstPos, stride);
        }
    }
}
