package be.twofold.tinybcdec;

public final class BC2Decoder extends BCDecoder {
    public BC2Decoder() {
        super(16, 4);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        BC1Decoder.decodeColor(src, srcPos, dst, dstPos, stride, true);
        decodeAlpha(src, srcPos, dst, dstPos + 3, stride);
    }

    private static void decodeAlpha(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        for (int y = 0, shift = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++, shift += 4) {
                dst[dstPos] = (byte) (((block >>> shift) & 0x0f) * 0x11);
                dstPos += 4;
            }
            dstPos += stride - 16;
        }
    }
}
