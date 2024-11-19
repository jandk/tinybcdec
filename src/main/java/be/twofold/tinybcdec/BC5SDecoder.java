package be.twofold.tinybcdec;

final class BC5SDecoder extends BlockDecoder {
    private final BC4SDecoder decoder = new BC4SDecoder(3);
    private final boolean normalized;

    BC5SDecoder(BlockFormat format) {
        super(format, 3);
        this.normalized = format == BlockFormat.BC5SignedNormalized;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decoder.decodeBlock(src, srcPos, dst, dstPos, stride);
        decoder.decodeBlock(src, srcPos + 8, dst, dstPos + 1, stride);

        if (normalized) {
            ReconstructZ.reconstruct(dst, dstPos, stride, bytesPerPixel);
        }
    }
}
