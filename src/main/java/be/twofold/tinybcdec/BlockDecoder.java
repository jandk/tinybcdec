package be.twofold.tinybcdec;

import java.util.*;

/**
 * This is the main class for decoding block compressed textures.
 * <p>
 * To create a new instance, use the {@link #create(BlockFormat, PixelOrder)} method.
 * <p>
 * To decode an entire image, use the {@link #decode(int, int, byte[], int)} or {@link #decode(int, int, byte[], int, byte[], int)} method.
 * Depending on if you want to allocate a new byte array or use an existing one.
 * <p>
 * To decode a single block, use the {@link #decodeBlock(byte[], int, byte[], int, int)} method.
 */
public abstract class BlockDecoder {
    static final int BLOCK_WIDTH = 4;
    static final int BLOCK_HEIGHT = 4;

    final BlockFormat format;
    final int bytesPerPixel;
    final int redOffset;
    final int greenOffset;
    final int blueOffset;
    final int alphaOffset;

    BlockDecoder(BlockFormat format, PixelOrder order) {
        this.format = format;
        this.bytesPerPixel = order.count() * format.bytesPerValue();
        this.redOffset = order.red() * format.bytesPerValue();
        this.greenOffset = order.green() * format.bytesPerValue();
        this.blueOffset = order.blue() * format.bytesPerValue();
        this.alphaOffset = order.alpha() * format.bytesPerValue();
    }

    /**
     * Creates a new block decoder for the given format and order.
     *
     * @param format The block format.
     * @param order  The channel order.
     * @return The block decoder.
     */
    public static BlockDecoder create(BlockFormat format, PixelOrder order) {
        if (format.minChannels() >= 1 && order.red() == -1) {
            throw new IllegalArgumentException("redChannel must be set for at least 1 byte per pixel");
        }
        if (format.minChannels() >= 2 && order.green() == -1) {
            throw new IllegalArgumentException("greenChannel must be set for at least 2 bytes per pixel");
        }
        if (format.minChannels() >= 3 && order.blue() == -1) {
            throw new IllegalArgumentException("blueChannel must be set for at least 3 bytes per pixel");
        }
        if (format.minChannels() >= 4 && order.alpha() == -1) {
            throw new IllegalArgumentException("alphaChannel must be set for at least 4 bytes per pixel");
        }
        switch (format) {
            case BC1:
                return new BC1Decoder(order, false);
            case BC2:
                return new BC2Decoder(order);
            case BC3:
                return new BC3Decoder(order);
            case BC4Unsigned:
                return new BC4UDecoder(order);
            case BC5Unsigned:
            case BC5UnsignedNormalized:
                return new BC5UDecoder(format, order);
            case BC6Unsigned:
            case BC6Signed:
                return new BC6Decoder(format, order);
            case BC7:
                return new BC7Decoder(order);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    /**
     * Decodes a single block of data.
     * The source data is expected to be in the format of the block format.
     * The destination data is expected to be in the format of the channel order.
     * <p>
     * There should be enough data/room in the source and the destination to read/write the block.
     *
     * @param src          The source data.
     * @param srcPos       The position in the source data.
     * @param dst          The destination data.
     * @param dstPos       The position in the destination data.
     * @param bytesPerLine The number of bytes per line in the destination data.
     */
    public abstract void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int bytesPerLine);

    /**
     * Decode an entire image, allocating a new byte array as the destination.
     *
     * @param width  The width of the image.
     * @param height The height of the image.
     * @param src    The source data.
     * @param srcPos The position in the source data.
     * @return The newly allocated decoded image.
     * @throws IllegalArgumentException  If the width or height is less than or equal to 0.
     * @throws IndexOutOfBoundsException If the source data is too small.
     */
    public byte[] decode(int width, int height, byte[] src, int srcPos) {
        byte[] dst = new byte[width * height * bytesPerPixel];
        decode(width, height, src, srcPos, dst, 0);
        return dst;
    }

    /**
     * Decode an entire image, using an existing byte array as the destination.
     *
     * @param width  The width of the image.
     * @param height The height of the image.
     * @param src    The source data.
     * @param srcPos The position in the source data.
     * @param dst    The destination data.
     * @param dstPos The position in the destination data.
     *               The destination data must have enough room to store the entire image.
     * @throws IllegalArgumentException  If the width or height is less than or equal to 0.
     * @throws IndexOutOfBoundsException If the source or destination data is too small.
     */
    public void decode(int width, int height, byte[] src, int srcPos, byte[] dst, int dstPos) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0");
        }

        int bytesPerLine = width * bytesPerPixel;
        Objects.checkFromIndexSize(srcPos, format.size(width, height), src.length);
        Objects.checkFromIndexSize(dstPos, height * bytesPerLine, dst.length);

        for (int y = 0; y < height; y += BLOCK_HEIGHT) {
            for (int x = 0; x < width; x += BLOCK_WIDTH, srcPos += format.bytesPerBlock()) {
                int dstOffset = dstPos + y * bytesPerLine + x * bytesPerPixel;
                if (height - y < BLOCK_HEIGHT || width - x < BLOCK_WIDTH) {
                    partialBlock(width, height, src, srcPos, dst, dstOffset, x, y, bytesPerLine);
                    continue;
                }

                decodeBlock(src, srcPos, dst, dstOffset, bytesPerLine);
                if (format.minChannels() < 4 && alphaOffset != -1) {
                    fillAlpha(dst, dstOffset, bytesPerLine);
                }
            }
        }
    }

    int rgba(int r, int g, int b, int a) {
        return (r << (redOffset * 8)) | (g << (greenOffset * 8)) | (b << (blueOffset * 8)) | (a << (alphaOffset * 8));
    }

    private void partialBlock(int width, int height, byte[] src, int srcPos, byte[] dst, int dstPos, int x, int y, int bytesPerLine) {
        int blockStride = BLOCK_WIDTH * bytesPerPixel;
        byte[] block = new byte[BLOCK_HEIGHT * blockStride];
        decodeBlock(src, srcPos, block, 0, blockStride);
        if (format.minChannels() < 4 && alphaOffset != -1) {
            fillAlpha(block, 0, blockStride);
        }

        int partialWidth = Math.min(width - x, BLOCK_WIDTH);
        int partialHeight = Math.min(height - y, BLOCK_HEIGHT);
        for (int yy = 0; yy < partialHeight; yy++) {
            System.arraycopy(
                block, yy * blockStride,
                dst, dstPos + yy * bytesPerLine,
                partialWidth * bytesPerPixel
            );
        }
    }

    private void fillAlpha(byte[] dst, int dstPos, int stride) {
        if (format == BlockFormat.BC6Signed || format == BlockFormat.BC6Unsigned) {
            fillAlphaShort(dst, dstPos, stride, (short) 0x3C00);
        } else {
            fillAlphaByte(dst, dstPos, stride, (byte) 0xFF);
        }
    }

    private void fillAlphaShort(byte[] dst, int dstPos, int stride, short value) {
        for (int yy = 0; yy < BLOCK_HEIGHT; yy++) {
            for (int xx = 0; xx < BLOCK_WIDTH; xx++) {
                ByteArrays.setShort(dst, dstPos + alphaOffset, value);
                dstPos += bytesPerPixel;
            }
            dstPos += stride - BLOCK_WIDTH * bytesPerPixel;
        }
    }

    private void fillAlphaByte(byte[] dst, int dstPos, int stride, byte value) {
        for (int yy = 0; yy < BLOCK_HEIGHT; yy++) {
            for (int xx = 0; xx < BLOCK_WIDTH; xx++) {
                dst[dstPos + alphaOffset] = value;
                dstPos += bytesPerPixel;
            }
            dstPos += stride - BLOCK_WIDTH * bytesPerPixel;
        }
    }
}
