package be.twofold.tinybcdec;

final class BC3Decoder extends BlockDecoder {
    private static final int BPP = 4;
    private static final BC1Decoder COLOR_DECODER = new BC1Decoder(BC1Decoder.Mode.BC2OR3);
    private static final BC4UDecoder ALPHA_DECODER = new BC4UDecoder(BPP);

    BC3Decoder() {
        super(BlockFormat.BC3, BPP);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        COLOR_DECODER.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        ALPHA_DECODER.decodeBlock(src, srcPos, dst, dstPos + 3, stride);
    }
}
