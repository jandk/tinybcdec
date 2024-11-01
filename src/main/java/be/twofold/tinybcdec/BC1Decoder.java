package be.twofold.tinybcdec;

final class BC1Decoder extends BCDecoder {
    BC1Decoder() {
        super(BlockFormat.BC1);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decodeColorBlock(src, srcPos, dst, dstPos, stride, false);
    }
}
