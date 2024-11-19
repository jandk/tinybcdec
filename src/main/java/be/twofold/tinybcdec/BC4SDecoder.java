package be.twofold.tinybcdec;

final class BC4SDecoder extends BCDecoder {
    BC4SDecoder() {
        super(BlockFormat.BC4Unsigned);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        decodeAlphaSigned(src, srcPos, dst, dstPos, stride);
    }
}
