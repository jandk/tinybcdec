package be.twofold.tinybcdec;

public final class BC5UDecoder extends BCDecoder {
    public BC5UDecoder(int bytesPerPixel) {
        super(16, bytesPerPixel);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        BC3Decoder.decodeAlpha(src, srcPos + 0, dst, dstPos + 0, stride, bytesPerPixel);
        BC3Decoder.decodeAlpha(src, srcPos + 8, dst, dstPos + 1, stride, bytesPerPixel);
    }
}
