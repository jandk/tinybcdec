package be.twofold.tinybcdec;

final class BC4SDecoder extends BlockDecoder {
    BC4SDecoder() {
        super(BlockFormat.BC4Unsigned);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        BCDecoder.decodeAlphaSigned(src, srcPos, dst, dstPos, stride);
        BCDecoder.expandGray(dst, dstPos, stride);
    }
}
