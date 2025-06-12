package be.twofold.tinybcdec;

final class BC2Decoder extends BlockDecoder {
    private static final int BPP = 4;
    private static final BC1Decoder COLOR_DECODER = new BC1Decoder(BC1Decoder.Mode.BC2OR3);

    BC2Decoder() {
        super(BlockFormat.BC2, BPP);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        COLOR_DECODER.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        decodeAlpha(src, srcPos, dst, dstPos + 3, stride);
    }

    private void decodeAlpha(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long alphas = ByteArrays.getLong(src, srcPos);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                byte alpha = (byte) ((alphas & 15) * 17);
                dst[dstPos + x * BPP] = alpha;
                alphas >>>= 4;
            }
            dstPos += stride;
        }
    }
}
