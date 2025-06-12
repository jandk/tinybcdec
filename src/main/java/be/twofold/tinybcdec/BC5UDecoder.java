package be.twofold.tinybcdec;

final class BC5UDecoder extends BlockDecoder {
    private static final int BPP = 3;
    private static final BC4UDecoder DECODER = new BC4UDecoder(BPP);

    private final boolean reconstructZ;

    BC5UDecoder(boolean reconstructZ) {
        super(reconstructZ ? BlockFormat.BC5U_RECONSTRUCT_Z : BlockFormat.BC5U, BPP);
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
