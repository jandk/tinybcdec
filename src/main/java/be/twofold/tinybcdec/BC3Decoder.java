package be.twofold.tinybcdec;

final class BC3Decoder extends BlockDecoder {
    private final BC1Decoder colorDecoder;
    private final BC4UDecoder alphaDecoder;

    BC3Decoder() {
        super(BlockFormat.BC3);
        this.colorDecoder = new BC1Decoder(true);
        this.alphaDecoder = new BC4UDecoder();
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos + 0, bytesPerLine);
        alphaDecoder.decodeBlock(src, srcPos, dst, dstPos + 3, bytesPerLine);
    }
}
