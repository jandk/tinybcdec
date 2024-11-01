package be.twofold.tinybcdec;

final class BC5SDecoder extends BlockDecoder {
    private final BC4SDecoder rDecoder;
    private final BC4SDecoder gDecoder;
    private final boolean normalized;

    BC5SDecoder(BlockFormat format) {
        super(format);
        this.rDecoder = new BC4SDecoder();
        this.gDecoder = new BC4SDecoder();
        this.normalized = format == BlockFormat.BC5SignedNormalized;
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
