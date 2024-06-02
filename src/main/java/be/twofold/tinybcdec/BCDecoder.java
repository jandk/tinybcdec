package be.twofold.tinybcdec;

import java.util.*;

public abstract class BCDecoder {
    private final int bytesPerBlock;
    final int bytesPerPixel;

    BCDecoder(int bytesPerBlock, int bytesPerPixel) {
        if (bytesPerPixel < 1 || bytesPerPixel > 6) {
            throw new IllegalArgumentException("bpp must be between 1 and 6");
        }
        this.bytesPerBlock = bytesPerBlock;
        this.bytesPerPixel = bytesPerPixel;
    }

    /**
     * Decodes a Block Compressed image.
     *
     * @param width  The width of the image.
     * @param height The height of the image.
     * @param src    The source data.
     */
    public byte[] decode(int width, int height, byte[] src, int srcPos) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0");
        }

        int widthInBlocks = (width + 3) / 4;
        int heightInBlocks = (height + 3) / 4;
        int expectedLength = widthInBlocks * heightInBlocks * bytesPerBlock;
        Objects.checkFromIndexSize(srcPos, expectedLength, src.length);

        int realWidth = widthInBlocks * 4;
        int realHeight = heightInBlocks * 4;
        int stride = realWidth * bytesPerPixel;
        int dstSize = realWidth * realHeight * bytesPerPixel;
        byte[] dst = new byte[dstSize];
        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4, srcPos += bytesPerBlock) {
                decodeBlock(src, srcPos, dst, y * stride + x * bytesPerPixel, stride);
            }
        }

        if (realWidth != width || realHeight != height) {
            byte[] result = new byte[width * height * bytesPerPixel];

            for (int y = 0, dstPos = 0; y < height; y++) {
                System.arraycopy(dst, srcPos, result, dstPos, width * bytesPerPixel);
                srcPos += realWidth * bytesPerPixel;
                dstPos += width * bytesPerPixel;
            }
            return result;
        }

        return dst;
    }

    /**
     * Decodes a single block.
     *
     * @param src    The source data. Must be 8 bytes long.
     * @param srcPos The position in the source data.
     * @param dst    The destination data. Must be at least 16 * bpr bytes long.
     * @param dstPos The position in the destination data.
     * @param stride The bytes per row in the destination data.
     */
    public abstract void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride);
}
