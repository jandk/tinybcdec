package be.twofold.tinybcdec;

final class BC3Decoder extends BCDecoder {
    private final BC1Decoder colorDecoder;
    private final BC4UDecoder alphaDecoder;

    BC3Decoder(BCOrder order) {
        super(BCFormat.BC3, order);
        this.colorDecoder = new BC1Decoder(order, true);
        this.alphaDecoder = new BC4UDecoder(BCOrder.single(order.count(), alphaOffset));
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        alphaDecoder.decodeBlock(src, srcPos, dst, dstPos, stride);
    }
}
