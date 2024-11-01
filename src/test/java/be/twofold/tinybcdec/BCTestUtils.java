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

        for (int i = 0; i < rawImage.length; i += 8) {
//            swap(rawImage, i + 0, i + 6);
//            swap(rawImage, i + 1, i + 7);
//            swap(rawImage, i + 2, i + 4);
//            swap(rawImage, i + 3, i + 5);
        }
        return rawImage;
    }

    private static byte[] decodeImage(byte[] image, int type) {
        switch (type) {
            case BufferedImage.TYPE_3BYTE_BGR: {
                byte[] result = new byte[image.length / 3 * 4];
                for (int i = 0, o = 0; i < image.length; i += 3, o += 4) {
                    result[o + 0] = image[i + 2];
                    result[o + 1] = image[i + 1];
                    result[o + 2] = image[i + 0];
                    result[o + 3] = (byte) 0xFF;
                }
                return result;
            }
            case BufferedImage.TYPE_4BYTE_ABGR: {
                for (int i = 0; i < image.length; i += 4) {
                    swap(image, i + 0, i + 3);
                    swap(image, i + 1, i + 2);
                }
                return image;
            }
            case BufferedImage.TYPE_BYTE_GRAY: {
                byte[] result = new byte[image.length * 4];
                for (int i = 0, o = 0; i < image.length; i++, o += 4) {
                    byte a = image[i];
                    result[o + 0] = a;
                    result[o + 1] = a;
                    result[o + 2] = a;
                    result[o + 3] = (byte) 0xFF;
                }
                return result;
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
