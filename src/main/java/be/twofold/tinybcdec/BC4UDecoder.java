package be.twofold.tinybcdec;

final class BC4UDecoder implements BlockDecoder {
    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst) {
        BC3Decoder.decodeAlpha(src, srcPos, dst, 0);
    }
}
