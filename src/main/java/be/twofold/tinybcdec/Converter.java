package be.twofold.tinybcdec;

import javafx.scene.image.*;

import java.awt.color.*;
import java.awt.image.*;
import java.nio.*;
import java.util.*;

/**
 * A converter for decoded block compressed textures.
 * <p>
 * This class provides methods to convert decoded block compressed textures to different image formats.
 * <p>
 * Use one of the static factory methods to create a converter for a specific image format.
 *
 * @param <T> The type of the image to convert to
 */
public abstract class Converter<T> {
    private static final byte[] Z = new byte[256 * 256];

    static {
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                float r = x * (1.0f / 127.5f) - 1.0f;
                float g = y * (1.0f / 127.5f) - 1.0f;
                float b = (float) Math.sqrt(1.0f - Math.min(1.0f, Math.max(r * r + g * g, 0.0f)));
                Z[(y << 8) + x] = (byte) (b * 127.5f + 128.0f);
            }
        }
    }

    Converter() {
    }

    /**
     * Returns a converter for AWT BufferedImage.
     *
     * @return A converter for AWT BufferedImage
     */
    public static Converter<BufferedImage> awt(boolean reconstructZ) {
        return new AWT(reconstructZ);
    }

    /**
     * Returns a converter for JavaFX Image.
     *
     * @return A converter for JavaFX Image
     */
    public static Converter<Image> fx(boolean reconstructZ) {
        return new FX(reconstructZ);
    }

    /**
     * Returns a converter for raw byte arrays.
     *
     * @return A converter for raw byte arrays
     */
    public static Converter<byte[]> raw() {
        return new Raw();
    }

    abstract T convert(byte[] decoded, int width, int height, int pixelStride);

    int swapRB(int pixel) {
        return Integer.rotateRight(Integer.reverseBytes(pixel), 8);
    }

    int reconstructZ(int r, int g) {
        return Byte.toUnsignedInt(Z[(g << 8) + r]);
    }

    private static final class AWT extends Converter<BufferedImage> {
        private final boolean reconstructZ;

        AWT(boolean reconstructZ) {
            this.reconstructZ = reconstructZ;
        }

        @Override
        BufferedImage convert(byte[] decoded, int width, int height, int pixelStride) {
            switch (pixelStride) {
                case 1:
                    return convertStride1(width, height, decoded);
                case 2:
                    return convertStride2(width, height, decoded);
                case 3:
                    return convertStride3(width, height, decoded);
                case 4:
                    return convertStride4(width, height, decoded);
                case 6:
                    return convertStride6(width, height, decoded);
                default:
                    throw new UnsupportedOperationException();
            }
        }

        private BufferedImage convertStride1(int width, int height, byte[] decoded) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            image.getRaster().setDataElements(0, 0, width, height, decoded);
            return image;
        }

        private BufferedImage convertStride2(int width, int height, byte[] decoded) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[] rawImage = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

            for (int i = 0, o = 0; i < decoded.length; i += 2, o++) {
                int r = Byte.toUnsignedInt(decoded[i/**/]);
                int g = Byte.toUnsignedInt(decoded[i + 1]);
                int b = reconstructZ ? reconstructZ(r, g) : 0;
                rawImage[o] = 0xFF000000 | r << 16 | g << 8 | b;
            }
            return image;
        }

        private BufferedImage convertStride3(int width, int height, byte[] decoded) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[] rawImage = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

            for (int i = 0, o = 0; i < decoded.length; i += 3, o++) {
                int r = Byte.toUnsignedInt(decoded[i/**/]);
                int g = Byte.toUnsignedInt(decoded[i + 1]);
                int b = Byte.toUnsignedInt(decoded[i + 2]);
                rawImage[o] = 0xFF000000 | r << 16 | g << 8 | b;
            }
            return image;
        }

        private BufferedImage convertStride4(int width, int height, byte[] decoded) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            int[] rawImage = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

            for (int i = 0, o = 0; i < decoded.length; i += 4, o++) {
                rawImage[o] = swapRB(ByteArrays.getInt(decoded, i));
            }
            return image;
        }

        private BufferedImage convertStride6(int width, int height, byte[] decoded) {
            ColorModel colorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
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
            for (int i = 0, o = 0; i < decoded.length; i += 2, o++) {
                rawImage[o] = Platform.float16ToFloat(ByteArrays.getShort(decoded, i));
            }
            return image;
        }
    }

    private static final class FX extends Converter<Image> {
        private final boolean reconstructZ;

        FX(boolean reconstructZ) {
            this.reconstructZ = reconstructZ;
        }

        @Override
        Image convert(byte[] decoded, int width, int height, int pixelStride) {
            IntBuffer out = IntBuffer.allocate(width * height);
            switch (pixelStride) {
                case 1:
                    convertStride1(decoded, out);
                    break;
                case 2:
                    convertStride2(decoded, out);
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

        private void convertStride2(byte[] decoded, IntBuffer out) {
            for (int i = 0, o = 0; i < decoded.length; i += 2, o++) {
                int r = Byte.toUnsignedInt(decoded[i/**/]);
                int g = Byte.toUnsignedInt(decoded[i + 1]);
                int b = reconstructZ ? reconstructZ(r, g) : 0;
                out.put(o, 0xFF000000 | r << 16 | g << 8 | b);
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

    private static final class Raw extends Converter<byte[]> {
        @Override
        byte[] convert(byte[] decoded, int width, int height, int pixelStride) {
            Objects.requireNonNull(decoded);
            return decoded;
        }
    }
}
