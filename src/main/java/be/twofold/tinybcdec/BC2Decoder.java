package be.twofold.tinybcdec;

final class BC2Decoder extends BCDecoder {
    private final BC1Decoder colorDecoder;
    private final int alphaChannel;

    BC2Decoder(int bytesPerPixel, int redChannel, int greenChannel, int blueChannel, int alphaChannel) {
        super(16, bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel);

        this.colorDecoder = new BC1Decoder(bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel, true);
        this.alphaChannel = alphaChannel;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        colorDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);
        decodeAlpha(src, srcPos, dst, dstPos + alphaChannel, stride);
    }

    private void decodeAlpha(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long alphas = ByteArrays.getLong(src, srcPos);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                dst[dstPos] = (byte) ((alphas & 0xf) * 0x11);
                alphas >>>= 4;
                dstPos += bytesPerPixel;
            }
            dstPos += stride - BLOCK_WIDTH * bytesPerPixel;
        }
    }
}
