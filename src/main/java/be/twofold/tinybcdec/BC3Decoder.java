package be.twofold.tinybcdec;

final class BC3Decoder extends BlockDecoder {
    private static final int BPP = 4;

    private final BC1Decoder colorDecoder = new BC1Decoder(BC1Decoder.Mode.BC2OR3);
    private final BC4UDecoder alphaDecoder = new BC4UDecoder(BPP);

    BC3Decoder() {
        super(BlockFormat.BC3, BPP);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        alphaDecoder.decodeBlock(src, srcPos, dst, dstPos + 3, stride);
    }
}
