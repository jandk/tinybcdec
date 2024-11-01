package be.twofold.tinybcdec;

final class BC2Decoder extends BlockDecoder {
    private final BC1Decoder colorDecoder;

    BC2Decoder() {
        super(BlockFormat.BC2);
        this.colorDecoder = new BC1Decoder(true);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, bytesPerLine);
        decodeAlpha(src, srcPos, dst, dstPos + 3, bytesPerLine);
    }

    private void decodeAlpha(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long alphas = ByteArrays.getLong(src, srcPos);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                dst[dstPos] = (byte) ((alphas & 0x0F) * 0x11);
                alphas >>>= 4;
                dstPos += BYTES_PER_PIXEL;
            }
            dstPos += stride - BLOCK_WIDTH * BYTES_PER_PIXEL;
        }
    }
}
