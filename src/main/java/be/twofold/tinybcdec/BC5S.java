package be.twofold.tinybcdec;

final class BC5S extends BlockDecoder {
    private static final int BPP = 3;
    private static final BC4S DECODER = new BC4S(BPP);

    private final boolean reconstructZ;

    BC5S(boolean reconstructZ) {
        super(BPP, 16);
        this.reconstructZ = reconstructZ;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        DECODER.decodeBlock(src, srcPos, dst, dstPos, stride);
        DECODER.decodeBlock(src, srcPos + 8, dst, dstPos + 1, stride);

        if (reconstructZ) {
            ReconstructZ.reconstruct(dst, dstPos, stride, BPP);
        }
    }
}
