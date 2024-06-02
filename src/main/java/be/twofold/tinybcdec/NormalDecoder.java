package be.twofold.tinybcdec;

public final class NormalDecoder extends BCDecoder {
    private static final byte[] Lut = initializeLut();

    public NormalDecoder(int bytesPerBlock, int bytesPerPixel) {
        super(bytesPerBlock, bytesPerPixel);
    }

    static byte lookup(byte r, byte g) {
        return Lut[Byte.toUnsignedInt(g) * 256 + Byte.toUnsignedInt(r)];
    }

    private static byte[] initializeLut() {
        var lut = new byte[256 * 256];
        for (var y = 0; y < 256; y++) {
            for (var x = 0; x < 256; x++) {
                var xx = unpackUNorm8Normal((byte) x);
                var yy = unpackUNorm8Normal((byte) y);
                var nz = (float) Math.sqrt(1.0f - clamp01(xx * xx + yy * yy));
                lut[y * 256 + x] = packUNorm8Normal(nz);
            }
        }
        return lut;
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        for (int y = 0, shift = 0; y < 4; y++) {
            for (var x = 0; x < 4; x++, shift += 3) {
                var r = dst[dstPos];
                var g = dst[dstPos + 1];
                dst[dstPos + 2] = lookup(r, g);
                dstPos += bytesPerPixel;
            }
            dstPos += stride - 4 * bytesPerPixel;
        }
    }

    public static float clamp01(float value) {
        return Math.min(1.0f, Math.max(value, 0.0f));
    }

    public static float unpackUNorm8Normal(byte value) {
        return Math.fma(Byte.toUnsignedInt(value) / 255.0f, 2.0f, -1.0f);
    }

    public static byte packUNorm8Normal(float value) {
        value = Math.fma(value, 0.5f, 0.5f);
        return (byte) Math.round(clamp01(value) * 255.0f);
    }

}
