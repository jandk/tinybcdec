package be.twofold.tinybcdec;

final class BC3Decoder extends BCDecoder {
    private final BC1Decoder colorDecoder;
    private final BC4UDecoder alphaDecoder;

    BC3Decoder(int bytesPerPixel, int redChannel, int greenChannel, int blueChannel, int alphaChannel) {
        super(16, bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel);
        this.colorDecoder = new BC1Decoder(bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel, true);
        this.alphaDecoder = new BC4UDecoder(bytesPerPixel, alphaChannel, -1, -1, -1);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        alphaDecoder.decodeBlock(src, srcPos, dst, dstPos, stride);
    }
}
