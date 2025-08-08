package be.twofold.tinybcdec;

import javafx.scene.image.*;

import java.nio.*;

final class ConverterFX extends Converter<Image> {
    @Override
    Image convert(int width, int height, byte[] decoded, int pixelStride) {
        IntBuffer out = IntBuffer.allocate(width * height);
        switch (pixelStride) {
            case 1:
                convertStride1(decoded, out);
                break;
            case 3:
                convertStride3(decoded, out);
                break;
            case 4:
                convertStride4(decoded, out);
                break;
            case 6:
                convertStride6(decoded, out);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return new WritableImage(new PixelBuffer<>(width, height, out, PixelFormat.getIntArgbPreInstance()));
    }

    private void convertStride1(byte[] decoded, IntBuffer out) {
        for (int i = 0; i < decoded.length; i++) {
            int b = Byte.toUnsignedInt(decoded[i]);
            out.put(i, 0xFF000000 | b * 0x010101);
        }
    }

    private void convertStride3(byte[] decoded, IntBuffer out) {
        for (int i = 0, o = 0; i < decoded.length; i += 3, o++) {
            int r = Byte.toUnsignedInt(decoded[i/**/]);
            int g = Byte.toUnsignedInt(decoded[i + 1]);
            int b = Byte.toUnsignedInt(decoded[i + 2]);
            out.put(o, 0xFF000000 | r << 16 | g << 8 | b);
        }
    }

    private void convertStride4(byte[] decoded, IntBuffer out) {
        for (int i = 0, o = 0; i < decoded.length; i += 4, o++) {
            out.put(o, swapRB(ByteArrays.getInt(decoded, i)));
        }
    }

    private void convertStride6(byte[] decoded, IntBuffer out) {
        for (int i = 0, o = 0; i < decoded.length; i += 6, o++) {
            int r = floatToSRGB(Platform.float16ToFloat(ByteArrays.getShort(decoded, i/**/)));
            int g = floatToSRGB(Platform.float16ToFloat(ByteArrays.getShort(decoded, i + 2)));
            int b = floatToSRGB(Platform.float16ToFloat(ByteArrays.getShort(decoded, i + 4)));
            out.put(o, 0xFF000000 | r << 16 | g << 8 | b);
        }
    }

    private int floatToSRGB(float f) {
        f = Math.min(1.0f, Math.max(f, 0.0f)); // clamp
        f = f > (0.04045f / 12.92f) // linear to sRGB
            ? (float) Math.pow(f, 1.0f / 2.4f) * 1.055f - 0.055f
            : f * 12.92f;
        return (int) (f * 255.0f + 0.5f); // scale and cast
    }
}
