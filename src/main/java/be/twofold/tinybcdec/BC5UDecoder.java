package be.twofold.tinybcdec;

public final class BC5UDecoder implements BlockDecoder {
    private static final byte[] NORMAL_LUT = initializeLut();
    private final boolean normalize;

    public BC5UDecoder(boolean normalize) {
        this.normalize = normalize;
    }

    private static byte lookup(byte r, byte g) {
        return NORMAL_LUT[Byte.toUnsignedInt(g) * 256 + Byte.toUnsignedInt(r)];
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

    private static float clamp01(float value) {
        return Math.min(1.0f, Math.max(value, 0.0f));
    }

    private static float unpackUNorm8Normal(byte value) {
        return Math.fma(Byte.toUnsignedInt(value) / 255.0f, 2.0f, -1.0f);
    }

    private static byte packUNorm8Normal(float value) {
        value = Math.fma(value, 0.5f, 0.5f);
        return (byte) Math.round(clamp01(value) * 255.0f);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst) {
        BC3Decoder.decodeAlpha(src, srcPos + 0, dst, 0);
        BC3Decoder.decodeAlpha(src, srcPos + 8, dst, 1);

        if (normalize) {
            for (var i = 0; i < 64; i += 4) {
                var r = dst[i + 0];
                var g = dst[i + 1];
                dst[i + 2] = lookup(r, g);
            }
        }
    }
}
