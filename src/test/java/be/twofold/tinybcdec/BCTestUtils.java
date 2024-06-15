package be.twofold.tinybcdec;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

final class BCTestUtils {
    public static final int DDS_HEADER_SIZE = 148;

    private BCTestUtils() {
    }

    static byte[] readResource(String path) throws IOException {
        try (InputStream in = BCTestUtils.class.getResourceAsStream(path)) {
            return in.readAllBytes();
        }
    }

    static byte[] readPng(String path) throws IOException {
        try (InputStream in = BCTestUtils.class.getResourceAsStream(path)) {
            BufferedImage image = ImageIO.read(in);
            return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        }
    }

    static byte[] readDDSFP16(String path, int width, int height) throws IOException {
        return Arrays.copyOfRange(readResource(path), DDS_HEADER_SIZE, DDS_HEADER_SIZE + width * height * 8);
    }
}
