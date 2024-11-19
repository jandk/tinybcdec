package be.twofold.tinybcdec;

import java.util.*;

/**
 * This is the main class for decoding block compressed textures.
 * <p>
 * To create a new instance, use the {@link #create(BlockFormat)} method.
 * <p>
 * To decode an entire image, use the {@link #decode(int, int, byte[], int)} or {@link #decode(int, int, byte[], int, byte[], int)} method.
 * Depending on if you want to allocate a new byte array or use an existing one.
 * <p>
 * To decode a single block, use the {@link #decodeBlock(byte[], int, byte[], int, int)} method.
 */
public abstract class BlockDecoder {
    private final BlockFormat format;
    final int bytesPerPixel;

    BlockDecoder(BlockFormat format) {
        this.format = format;
        this.bytesPerPixel = format.bytesPerPixel();
    }

    /**
     * Creates a new block decoder for the given format and order.
     *
     * @param format The block format.
     * @return The block decoder.
     */
    public static BlockDecoder create(BlockFormat format) {
        switch (format) {
            case BC1:
                return new BC1Decoder();
            case BC2:
                return new BC2Decoder();
            case BC3:
                return new BC3Decoder();
            case BC4Unsigned:
                return new BC4UDecoder();
            case BC4Signed:
                return new BC4SDecoder();
            case BC5Unsigned:
            case BC5UnsignedNormalized:
                return new BC5UDecoder(format);
            case BC5Signed:
            case BC5SignedNormalized:
                return new BC5SDecoder(format);
            case BC6Unsigned:
            case BC6Signed:
                return new BC6Decoder(format);
            case BC7:
                return new BC7Decoder();
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
     * @param src    The source data.
     * @param srcPos The position in the source data.
     * @param dst    The destination data.
     * @param dstPos The position in the destination data.
     * @param stride The number of bytes per line in the destination data.
     */
    public abstract void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride);

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

        for (int y = 0; y < height; y += BCDecoder.BLOCK_HEIGHT) {
            for (int x = 0; x < width; x += BCDecoder.BLOCK_WIDTH, srcPos += format.bytesPerBlock()) {
                int dstOffset = dstPos + y * bytesPerLine + x * bytesPerPixel;
                if (height - y < BCDecoder.BLOCK_HEIGHT || width - x < BCDecoder.BLOCK_WIDTH) {
                    partialBlock(width, height, src, srcPos, dst, dstOffset, x, y, bytesPerLine);
                    continue;
                }

                decodeBlock(src, srcPos, dst, dstOffset, bytesPerLine);
            }
        }
    }

    /**
     * Decode an entire image.
     *
     * @param width     The width of the image.
     * @param height    The height of the image.
     * @param src       The source data.
     * @param srcPos    The position in the source data.
     * @param converter The converter that's used to handle the result.
     *                  Can be used to create a new AWT or JavaFX image.
     * @return The newly allocated decoded and converted image.
     * @throws IllegalArgumentException  If the width or height is less than or equal to 0.
     * @throws IndexOutOfBoundsException If the source data is too small.
     * @see be.twofold.tinybcdec.Converter.AWT
     * @see be.twofold.tinybcdec.Converter.FX
     */
    public <T> T decode(int width, int height, byte[] src, int srcPos, Converter<T> converter) {
        byte[] decoded = decode(width, height, src, srcPos);
        return converter.convert(width, height, decoded, format);
    }

    private void partialBlock(int width, int height, byte[] src, int srcPos, byte[] dst, int dstPos, int x, int y, int bytesPerLine) {
        int blockStride = BCDecoder.BLOCK_WIDTH * bytesPerPixel;
        byte[] block = new byte[BCDecoder.BLOCK_HEIGHT * blockStride];
        decodeBlock(src, srcPos, block, 0, blockStride);

        int partialWidth = Math.min(width - x, BCDecoder.BLOCK_WIDTH);
        int partialHeight = Math.min(height - y, BCDecoder.BLOCK_HEIGHT);
        for (int yy = 0; yy < partialHeight; yy++) {
            System.arraycopy(
                block, yy * blockStride,
                dst, dstPos + yy * bytesPerLine,
                partialWidth * bytesPerPixel
            );
        }
    }
}
