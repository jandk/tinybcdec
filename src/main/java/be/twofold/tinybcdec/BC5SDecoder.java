package be.twofold.tinybcdec;

final class BC5SDecoder extends BlockDecoder {
    private static final int BPP = 3;

    private final BC4SDecoder decoder = new BC4SDecoder(BPP);
    private final boolean reconstructZ;

    BC5SDecoder(boolean reconstructZ) {
        super(reconstructZ ? BlockFormat.BC5S_RECONSTRUCT_Z : BlockFormat.BC5S, BPP);
        this.reconstructZ = reconstructZ;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decoder.decodeBlock(src, srcPos, dst, dstPos, stride);
        decoder.decodeBlock(src, srcPos + 8, dst, dstPos + 1, stride);

        if (reconstructZ) {
            ReconstructZ.reconstruct(dst, dstPos, stride, BPP);
        }
    }
}
