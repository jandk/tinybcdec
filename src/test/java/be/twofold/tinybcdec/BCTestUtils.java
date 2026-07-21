package be.twofold.tinybcdec;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.nio.*;

public final class BCTestUtils {
    public static final int DDS_HEADER_SIZE = 148;

    private BCTestUtils() {
    }

    public static ByteBuffer readResource(String path) throws IOException {
        try (InputStream in = BCTestUtils.class.getResourceAsStream(path)) {
            return ByteBuffer.wrap(in.readAllBytes());
        }
    }

    /**
     * This conversion is actually just a byte level re-arrange and gray expansion.
     */
    static ByteBuffer readPng(String path, boolean rgba) throws IOException {
        try (InputStream in = BCTestUtils.class.getResourceAsStream(path)) {
            BufferedImage image = ImageIO.read(in);
            int width = image.getWidth();
            int height = image.getHeight();

            BufferedImage argb = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = argb.createGraphics();
            graphics.setComposite(AlphaComposite.Src); // Needed for proper alpha copy
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();

            int[] pixels = ((DataBufferInt) argb.getRaster().getDataBuffer()).getData();
            ByteBuffer result = ByteBuffer.allocate(pixels.length * 4).order(ByteOrder.LITTLE_ENDIAN);
            result.asIntBuffer().put(pixels);

            if (rgba) {
                for (int i = 0; i < result.remaining(); i += 4) {
                    byte temp = result.get(i);
                    result.put(i, result.get(i + 2));
                    result.put(i + 2, temp);
                }
            }

            return result.order(ByteOrder.BIG_ENDIAN);
        }
    }

    static ByteBuffer readDDSFP16(String path, int width, int height) throws IOException {
        return readResource(path)
            .position(DDS_HEADER_SIZE)
            .limit(DDS_HEADER_SIZE + width * height * 8)
            .slice();
    }

    /**
     * Compares two buffers byte for byte, starting at their current positions.
     * Neither position is modified.
     */
    static void assertBufferEquals(ByteBuffer actual, ByteBuffer expected) {
        int actualBase = actual.position();
        int expectedBase = expected.position();
        int length = expected.remaining();

        if (actual.remaining() != length) {
            throw new AssertionError("Expected " + length + " bytes, but was " + actual.remaining());
        }
        for (int i = 0; i < length; i++) {
            byte a = actual.get(actualBase + i);
            byte e = expected.get(expectedBase + i);
            if (a != e) {
                throw new AssertionError(String.format(
                    "Buffers differ at index %d: expected 0x%02X but was 0x%02X", i, e, a));
            }
        }
    }

    /**
     * Compares two buffers float by float, starting at their current positions.
     * Neither position is modified.
     */
    static void assertBufferEqualsFloats(ByteBuffer actual, ByteBuffer expected) {
        int actualBase = actual.position();
        int expectedBase = expected.position();
        int length = expected.remaining();

        if (actual.remaining() != length * 4) {
            throw new AssertionError("Expected " + length * 4 + " bytes, but was " + actual.remaining());
        }
        for (int i = 0; i < length; i++) {
            float a = actual.getFloat(actualBase + i * 4);
            float e = Byte.toUnsignedInt(expected.get(expectedBase + i)) / 255.0f;
            if (Math.abs(a - e) > 0.01f) {
                throw new AssertionError(String.format(
                    "Buffers differ at index %d: expected %.6f but was %.6f", i, e, a));
            }
        }
    }
}
