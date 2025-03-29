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
            byte[] rawImage = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

            return decodeImage(rawImage, image.getType());
        }
    }

    static byte[] readDDSFP16(String path, int width, int height) throws IOException {
        byte[] rawImage = Arrays.copyOfRange(readResource(path), DDS_HEADER_SIZE, DDS_HEADER_SIZE + width * height * 8);
        byte[] result = new byte[rawImage.length * 6 / 8];

        for (int i = 0, o = 0; i < rawImage.length; i += 8, o += 6) {
            System.arraycopy(rawImage, i, result, o, 6);
        }
        return result;
    }

    private static byte[] decodeImage(byte[] image, int type) {
        switch (type) {
            case BufferedImage.TYPE_3BYTE_BGR: {
                for (int i = 0; i < image.length; i += 3) {
                    swap(image, i/**/, i + 2);
                }
                return image;
            }
            case BufferedImage.TYPE_4BYTE_ABGR: {
                for (int i = 0; i < image.length; i += 4) {
                    swap(image, i/**/, i + 3);
                    swap(image, i + 1, i + 2);
                }
                return image;
            }
            case BufferedImage.TYPE_BYTE_GRAY: {
                return image;
            }
            default:
                throw new UnsupportedOperationException(type + " not supported");
        }
    }

    private static void swap(byte[] array, int i, int j) {
        byte tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
}
