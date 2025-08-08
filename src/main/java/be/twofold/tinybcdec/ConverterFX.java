package be.twofold.tinybcdec;

import javafx.scene.image.*;

import java.nio.*;

final class ConverterFX extends Converter<Image> {
    @Override
    Image convert(int width, int height, byte[] decoded, int pixelStride) {
        ByteBuffer in = ByteBuffer.wrap(decoded).order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer out = IntBuffer.allocate(width * height);
        switch (pixelStride) {
            case 1:
                convertStride1(decoded, out);
                break;
            case 3:
                convertStride3(decoded, out);
                break;
            case 4:
                convertStride4(in, out);
                break;
            case 6:
                convertStride6(in, out);
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

    private void convertStride4(ByteBuffer in, IntBuffer out) {
        IntBuffer ints = in.asIntBuffer();
        for (int i = 0, len = ints.remaining(); i < len; i++) {
            out.put(i, swapRB(ints.get(i)));
        }
    }

    private void convertStride6(ByteBuffer in, IntBuffer out) {
        ShortBuffer shorts = in.asShortBuffer();
        for (int i = 0, o = 0, len = in.remaining(); i < len; i += 3, o++) {
            int r = floatToSRGB(Platform.float16ToFloat(shorts.get(i/**/)));
            int g = floatToSRGB(Platform.float16ToFloat(shorts.get(i + 1)));
            int b = floatToSRGB(Platform.float16ToFloat(shorts.get(i + 2)));
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
