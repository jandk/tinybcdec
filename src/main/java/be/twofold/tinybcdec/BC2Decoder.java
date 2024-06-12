package be.twofold.tinybcdec;

public final class BC2Decoder implements BlockDecoder {
    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst) {
        BC1Decoder.decodeColor(src, srcPos + 8, dst, true);
        decodeAlpha(src, srcPos, dst);
    }

    private static void decodeAlpha(byte[] src, int srcPos, byte[] dst) {
        long block = ByteArrays.getLong(src, srcPos);

        for (int i = 0; i < 16; i++) {
            dst[(i * 4) + 3] = (byte) (((block >>> (i * 4)) & 0x0f) * 0x11);
        }
    }
}
