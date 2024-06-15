package be.twofold.tinybcdec;

final class BC5UDecoder extends BCDecoder {
    private final BC4UDecoder rDecoder;
    private final BC4UDecoder gDecoder;
    private final boolean normalize;

    BC5UDecoder(int bytesPerPixel, int rOffset, int gOffset) {
        super(16, 2, bytesPerPixel, rOffset, gOffset, -1, -1);
        this.rDecoder = new BC4UDecoder(bytesPerPixel, rOffset);
        this.gDecoder = new BC4UDecoder(bytesPerPixel, gOffset);
        this.normalize = false;
    }

    BC5UDecoder(int bytesPerPixel, int rOffset, int gOffset, int bOffset) {
        super(16, 3, bytesPerPixel, rOffset, gOffset, bOffset, -1);
        this.rDecoder = new BC4UDecoder(bytesPerPixel, rOffset);
        this.gDecoder = new BC4UDecoder(bytesPerPixel, gOffset);
        this.normalize = true;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        rDecoder.decodeBlock(src, srcPos, dst, dstPos, stride);
        gDecoder.decodeBlock(src, srcPos + 8, dst, dstPos, stride);

        if (normalize) {
            for (int y = 0; y < BLOCK_HEIGHT; y++) {
                for (int x = 0; x < BLOCK_WIDTH; x++) {
                    byte r = dst[dstPos + rOffset];
                    byte g = dst[dstPos + gOffset];
                    dst[dstPos + bOffset] = NormalDecoder.decode(r, g);
                    dstPos += bytesPerPixel;
                }
                dstPos += stride - BLOCK_WIDTH * bytesPerPixel;
            }
        }
    }
}
