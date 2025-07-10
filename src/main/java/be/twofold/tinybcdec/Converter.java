package be.twofold.tinybcdec;

import javafx.scene.image.*;

import java.awt.image.*;

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
    Converter() {
    }

    /**
     * Returns a converter for AWT BufferedImage.
     *
     * @return A converter for AWT BufferedImage
     */
    public static Converter<BufferedImage> awt() {
        return new ConverterAWT();
    }

    /**
     * Returns a converter for JavaFX Image.
     *
     * @return A converter for JavaFX Image
     */
    public static Converter<Image> fx() {
        return new ConverterFX();
    }

    /**
     * Returns a converter for raw byte arrays.
     *
     * @return A converter for raw byte arrays
     */
    public static Converter<byte[]> raw() {
        return new ConverterRaw();
    }

    abstract T convert(int width, int height, byte[] decoded, int pixelStride);

    int swapRB(int pixel) {
        return Integer.rotateRight(Integer.reverseBytes(pixel), 8);
    }
}
