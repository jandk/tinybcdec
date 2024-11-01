package be.twofold.tinybcdec;

import javafx.scene.image.*;

import java.awt.color.*;
import java.awt.image.*;
import java.nio.*;

public interface Converter<T> {
    private static float float16ToFloat(short half) {
        // Extract the separate fields
        int s = (half & 0x8000) << 16;
        int e = (half >> 10) & 0x001F;
        int m = half & 0x03FF;

        // Zero and denormal numbers, copies the sign
        if (e == 0) {
            float sign = Float.intBitsToFloat(s | 0x3F800000);
            return sign * (0x1p-24f * m); // Smallest denormal in float16
        }

        // Infinity and NaN, propagate the mantissa for signalling NaN
        if (e == 31) {
            return Float.intBitsToFloat(s | 0x7F800000 | m << 13);
        }

        // Adjust exponent, and put everything back together
        e = e + (127 - 15);
        return Float.intBitsToFloat(s | e << 23 | m << 13);
    }

    private static int swapRB(int pixel) {
        return pixel & 0xFF00FF00
            | (pixel & 0x00FF0000) >> 16
            | (pixel & 0x000000FF) << 16;
    }

    T convert(int width, int height, byte[] decoded, BlockFormat format);

    final class AWT implements Converter<BufferedImage> {
        @Override
        public BufferedImage convert(int width, int height, byte[] decoded, BlockFormat format) {
            if (format == BlockFormat.BC6Signed || format == BlockFormat.BC6Unsigned) {
                ColorModel colorModel = new ComponentColorModel(
                    ColorSpace.getInstance(ColorSpace.CS_sRGB),
                    false,
                    false,
                    ColorModel.OPAQUE,
                    DataBuffer.TYPE_FLOAT
                );
                BufferedImage image = new BufferedImage(
                    colorModel,
                    colorModel.createCompatibleWritableRaster(width, height),
                    colorModel.isAlphaPremultiplied(),
                    null
                );

                float[] rawImage = ((DataBufferFloat) image.getRaster().getDataBuffer()).getData();
                for (int i = 0, o = 0, len = decoded.length; i < len; i += 8, o += 3) {
                    rawImage[o + 0] = float16ToFloat(ByteArrays.getShort(decoded, i + 0));
                    rawImage[o + 1] = float16ToFloat(ByteArrays.getShort(decoded, i + 2));
                    rawImage[o + 2] = float16ToFloat(ByteArrays.getShort(decoded, i + 4));
                }
                return image;
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);

            int[] rawImage = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            for (int i = 0, o = 0, len = decoded.length; i < len; i += 4, o++) {
                int pixel = ByteArrays.getInt(decoded, i);
                rawImage[o] = swapRB(pixel);
            }
            return image;
        }
    }

    final class FX implements Converter<Image> {
        private static int clampAndPack(float value) {
            return (int) Math.fma(Math.min(Math.max(value, 0.0f), 1.0f), 255.0f, 0.5f);
        }

        @Override
        public Image convert(int width, int height, byte[] decoded, BlockFormat format) {
            IntBuffer buffer = IntBuffer.allocate(width * height);
            if (format == BlockFormat.BC6Signed || format == BlockFormat.BC6Unsigned) {
                for (int i = 0, o = 0, len = decoded.length; i < len; i += 8, o++) {
                    int r = clampAndPack(float16ToFloat(ByteArrays.getShort(decoded, i + 0)));
                    int g = clampAndPack(float16ToFloat(ByteArrays.getShort(decoded, i + 2)));
                    int b = clampAndPack(float16ToFloat(ByteArrays.getShort(decoded, i + 4)));
                    buffer.put(0xFF000000 | r << 16 | g << 8 | b);
                }
            } else {
                for (int i = 0, o = 0, len = decoded.length; i < len; i += 4, o++) {
                    int pixel = ByteArrays.getInt(decoded, i);
                    buffer.put(swapRB(pixel));
                }
            }
            return new WritableImage(new PixelBuffer<>(width, height, buffer, PixelFormat.getIntArgbPreInstance()));
        }
    }

    final class Raw implements Converter<byte[]> {
        @Override
        public byte[] convert(int width, int height, byte[] decoded, BlockFormat format) {
            return decoded;
        }
    }
}
