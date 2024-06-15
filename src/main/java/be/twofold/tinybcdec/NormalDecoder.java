package be.twofold.tinybcdec;

final class NormalDecoder {
    private static final byte[] LUT = new byte[256 * 256];

    static {
        for (var y = 0; y < 256; y++) {
            for (var x = 0; x < 256; x++) {
                // Normalize x and y to [-1, 1]
                var xx = Math.fma(x, (1.0f / 127.5f), -1.0f);
                var yy = Math.fma(y, (1.0f / 127.5f), -1.0f);
                var sq = xx * xx + yy * yy;
                var nz = (float) Math.sqrt(1.0f - Math.min(1.0f, Math.max(sq, 0.0f)));
                LUT[y * 256 + x] = (byte) Math.fma(nz, 127.5f, 128.0f);
            }
        }
    }

    private NormalDecoder() {
    }

    static byte decode(byte r, byte g) {
        return LUT[Byte.toUnsignedInt(g) * 256 + Byte.toUnsignedInt(r)];
    }
}
