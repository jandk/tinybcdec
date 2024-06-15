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

    static void compareBC(byte[] actual, byte[] expected, int ro, int go, int bo, int ao) {
        Assertions.assertThat(actual).hasSameSizeAs(expected);

        for (int i = 0; i < expected.length; i += 4) {
            int r1 = Byte.toUnsignedInt(actual[i + ro]);
            int g1 = Byte.toUnsignedInt(actual[i + go]);
            int b1 = Byte.toUnsignedInt(actual[i + bo]);
            int a1 = Byte.toUnsignedInt(actual[i + ao]);

            int r0 = Byte.toUnsignedInt(expected[i + ro]);
            int g0 = Byte.toUnsignedInt(expected[i + go]);
            int b0 = Byte.toUnsignedInt(expected[i + bo]);
            int a0 = Byte.toUnsignedInt(expected[i + ao]);

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
            return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        }
    }

    static byte[] readDDSFP16(String path, int width, int height) throws IOException {
        return Arrays.copyOfRange(readResource(path), DDS_HEADER_SIZE, DDS_HEADER_SIZE + width * height * 8);
    }
}
