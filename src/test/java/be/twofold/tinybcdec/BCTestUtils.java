package be.twofold.tinybcdec;

import org.assertj.core.api.*;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

final class BCTestUtils {

    public static final int DDS_HEADER_SIZE = 148;

    private BCTestUtils() {
    }

    static void compareBC(byte[] actual, byte[] expected) {
        Assertions.assertThat(actual).hasSameSizeAs(expected);

        for (int i = 0; i < expected.length; i += 4) {
            int r1 = Byte.toUnsignedInt(actual[i + 0]);
            int g1 = Byte.toUnsignedInt(actual[i + 1]);
            int b1 = Byte.toUnsignedInt(actual[i + 2]);
            int a1 = Byte.toUnsignedInt(actual[i + 3]);

            int r0 = Byte.toUnsignedInt(expected[i + 0]);
            int g0 = Byte.toUnsignedInt(expected[i + 1]);
            int b0 = Byte.toUnsignedInt(expected[i + 2]);
            int a0 = Byte.toUnsignedInt(expected[i + 3]);

            if (Math.abs(r0 - r1) > 1 || Math.abs(g0 - g1) > 1 || Math.abs(b0 - b1) > 1 || a0 != a1) {
                fail("i: " + i + " r0: " + r0 + " g0: " + g0 + " b0: " + b0 + " a0: " + a0 + " r1: " + r1 + " g1: " + g1 + " b1: " + b1 + " a1: " + a1);
            }
        }
    }

    static byte[] readResource(String path) throws IOException {
        try (var in = BCTestUtils.class.getResourceAsStream(path)) {
            return in.readAllBytes();
        }
    }

    static byte[] readPng(String path) throws IOException {
        try (var in = BCTestUtils.class.getResourceAsStream(path)) {
            BufferedImage image = ImageIO.read(in);
            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                swizzle4(data);
            }
            if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                swizzle3(data);
            }
            return data;
        }
    }

    static byte[] readDDSFP16(String path, int width, int height) throws IOException {
        byte[] read = Arrays.copyOfRange(readResource(path), DDS_HEADER_SIZE, DDS_HEADER_SIZE + width * height * 8);

        byte[] expected = new byte[read.length * 3 / 4];
        for (int i = 0, j = 0; i < read.length; i += 8, j += 6) {
            System.arraycopy(read, i, expected, j, 6);
        }
        return expected;
    }

    private static void swizzle3(byte[] data) {
        for (int i = 0; i < data.length; i += 3) {
            swap(data, i + 0, i + 2);
        }
    }

    private static void swizzle4(byte[] array) {
        for (int i = 0; i < array.length; i += 4) {
            swap(array, i + 0, i + 3);
            swap(array, i + 1, i + 2);
        }
    }

    private static void swap(byte[] array, int i, int j) {
        byte tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
}
