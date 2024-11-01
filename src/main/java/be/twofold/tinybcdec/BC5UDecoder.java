package be.twofold.tinybcdec;

final class BC5UDecoder extends BlockDecoder {
    private final BC4UDecoder rDecoder = new BC4UDecoder();
    private final BC4UDecoder gDecoder = new BC4UDecoder();
    private final boolean normalized;

    BC5UDecoder(BlockFormat format) {
        super(format);
        this.normalized = format == BlockFormat.BC5UnsignedNormalized;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine) {
        rDecoder.decodeBlock(src, srcPos, dst, dstPos + 0, bytesPerLine);
        gDecoder.decodeBlock(src, srcPos + 8, dst, dstPos + 1, bytesPerLine);

        if (normalized) {
            ReconstructZ.reconstruct(dst, dstPos, bytesPerLine);
        }
    }
}
