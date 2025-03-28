package be.twofold.tinybcdec;

final class BC5UDecoder extends BlockDecoder {
    private static final int BPP = 3;

    private final BC4UDecoder decoder = new BC4UDecoder(BPP);
    private final boolean normalized;

    BC5UDecoder(BlockFormat format) {
        super(format, BPP);
        this.normalized = format == BlockFormat.BC5U_RECONSTRUCT_Z;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decoder.decodeBlock(src, srcPos, dst, dstPos, stride);
        decoder.decodeBlock(src, srcPos + 8, dst, dstPos + 1, stride);

        if (normalized) {
            ReconstructZ.reconstruct(dst, dstPos, stride, BPP);
        }
    }
}
