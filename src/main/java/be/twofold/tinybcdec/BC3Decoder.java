package be.twofold.tinybcdec;

final class BC3Decoder extends BlockDecoder {
    private final BC1Decoder colorDecoder;
    private final BC4UDecoder alphaDecoder;

    public BC3Decoder(int bytesPerPixel, int rOffset, int gOffset, int bOffset, int aOffset) {
        super(16, 4, bytesPerPixel, rOffset, gOffset, bOffset, aOffset);
        this.colorDecoder = new BC1Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset, true);
        this.alphaDecoder = new BC4UDecoder(bytesPerPixel, aOffset);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        alphaDecoder.decodeBlock(src, srcPos, dst, dstPos, stride);
    }
}
