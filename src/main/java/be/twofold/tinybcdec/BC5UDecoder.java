package be.twofold.tinybcdec;

final class BC5UDecoder extends BlockDecoder {
    private final BC4UDecoder rDecoder;
    private final BC4UDecoder gDecoder;
    private final boolean normalized;

    BC5UDecoder(BlockFormat format, PixelOrder order) {
        super(format, order);
        this.rDecoder = new BC4UDecoder(PixelOrder.single(order.count(), redOffset));
        this.gDecoder = new BC4UDecoder(PixelOrder.single(order.count(), greenOffset));
        this.normalized = format == BlockFormat.BC5UnsignedNormalized;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine) {
        rDecoder.decodeBlock(src, srcPos, dst, dstPos, bytesPerLine);
        gDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, bytesPerLine);

        if (normalized) {
            ReconstructZ.reconstruct(dst, dstPos, bytesPerLine, bytesPerPixel, redOffset, greenOffset, blueOffset);
        }
    }
}
