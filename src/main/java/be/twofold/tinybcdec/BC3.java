package be.twofold.tinybcdec;

final class BC3 extends BlockDecoder {
    static final BC3 INSTANCE = new BC3();

    private static final int BPP = 4;
    private static final BC1 COLOR_DECODER = new BC1(BC1.Mode.BC2OR3);
    private static final BC4U ALPHA_DECODER = new BC4U(BPP);

    private BC3() {
        super(BPP, 16);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        COLOR_DECODER.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        ALPHA_DECODER.decodeBlock(src, srcPos, dst, dstPos + 3, stride);
    }
}
