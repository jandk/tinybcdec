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
    static ByteBuffer readPng(String path) throws IOException {
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
}
