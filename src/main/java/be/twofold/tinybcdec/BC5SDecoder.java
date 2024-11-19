package be.twofold.tinybcdec;

final class BC5SDecoder extends BCDecoder {
    private final boolean normalized;

    BC5SDecoder(BlockFormat format) {
        super(format);
        this.normalized = format == BlockFormat.BC5SignedNormalized;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decodeAlphaSigned(src, srcPos, dst, dstPos, stride);
        decodeAlphaSigned(src, srcPos + 8, dst, dstPos + 1, stride);
        fillAlpha(dst, dstPos, stride);

        if (normalized) {
            ReconstructZ.reconstruct(dst, dstPos, stride, bytesPerPixel);
        }
    }
}
