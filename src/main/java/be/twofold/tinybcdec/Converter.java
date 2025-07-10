package be.twofold.tinybcdec;

import be.twofold.tinybcdec.converter.*;
import javafx.scene.image.*;

import java.awt.image.*;

public abstract class Converter<T> {
    protected Converter() {
    }

    public static Converter<BufferedImage> awt() {
        return new AWTConverter();
    }

    public static Converter<Image> fx() {
        return new FXConverter();
    }

    public static Converter<byte[]> raw() {
        return new RawConverter();
    }

    protected abstract T convert(int width, int height, byte[] decoded, int pixelStride);

    protected int swapRB(int pixel) {
        return Integer.rotateRight(Integer.reverseBytes(pixel), 8);
    }
}
