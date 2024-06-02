package be.twofold.tinybcdec;

public final class BC4UDecoder extends BCDecoder {
    public BC4UDecoder() {
        super(8, 1);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        BC3Decoder.decodeAlpha(src, srcPos, dst, dstPos, stride, 1);
    }
}
