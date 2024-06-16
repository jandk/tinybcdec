package be.twofold.tinybcdec;

final class BC3Decoder extends BlockDecoder {
    private final BC1Decoder colorDecoder;
    private final BC4UDecoder alphaDecoder;

    BC3Decoder(PixelOrder order) {
        super(BlockFormat.BC3, order);
        this.colorDecoder = new BC1Decoder(order, true);
        this.alphaDecoder = new BC4UDecoder(PixelOrder.single(order.count(), alphaOffset));
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, bytesPerLine);
        alphaDecoder.decodeBlock(src, srcPos, dst, dstPos, bytesPerLine);
    }
}
