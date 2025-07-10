package be.twofold.tinybcdec;

import java.util.*;

/**
 * This is the main class for decoding block compressed textures.
 * <p>
 * To create a new instance, use one of the static factory methods starting with {@code bc}.
 * <p>
 * To decode an entire image, use the {@link #decode(int, int, byte[], int)} or {@link #decode(int, int, byte[], int, byte[], int)} method.
 * Depending on if you want to allocate a new byte array or use an existing one.
 * <p>
 * To decode a single block, use the {@link #decodeBlock(byte[], int, byte[], int, int)} method.
 */
public abstract class BlockDecoder {
    static final int BLOCK_WIDTH = 4;
    static final int BLOCK_HEIGHT = 4;

    final int pixelStride;
    final int bytesPerBlock;

    BlockDecoder(int pixelStride, int bytesPerBlock) {
        this.pixelStride = pixelStride;
        this.bytesPerBlock = bytesPerBlock;
    }

    /**
     * Returns a block decoder for BC1.
     *
     * @param opaque Whether to decode the image as opaque or not.
     * @return The block decoder.
     */
    public static BlockDecoder bc1(boolean opaque) {
        return new BC1(opaque ? BC1.Mode.OPAQUE : BC1.Mode.NORMAL);
    }

    /**
     * Returns a block decoder for BC2.
     *
     * @return The block decoder.
     */
    public static BlockDecoder bc2() {
        return BC2.INSTANCE;
    }

    /**
     * Returns a block decoder for BC3.
     *
     * @return The block decoder.
     */
    public static BlockDecoder bc3() {
        return BC3.INSTANCE;
    }

    /**
     * Returns a block decoder for BC4.
     *
     * @param signed Whether to interpret the data as signed or unsigned.
     * @return The block decoder.
     */
    public static BlockDecoder bc4(boolean signed) {
        return signed ? new BC4S(1) : new BC4U(1);
    }

    /**
     * Returns a block decoder for BC5.
     *
     * @param signed       Whether to interpret the data as signed or unsigned.
     * @param reconstructZ Whether to reconstruct the Z component or not.
     * @return The block decoder.
     */
    public static BlockDecoder bc5(boolean signed, boolean reconstructZ) {
        return signed ? new BC5S(reconstructZ) : new BC5U(reconstructZ);
    }

    /**
     * Returns a block decoder for BC6H.
     *
     * @param signed  Whether to interpret the data as signed or unsigned.
     * @param asFloat Whether to interpret the data as float or half float.
     * @return The block decoder.
     */
    public static BlockDecoder bc6h(boolean signed, boolean asFloat) {
        return new BC6H(signed, asFloat);
    }

    /**
     * Returns a block decoder for BC7.
     *
     * @return The block decoder.
     */
    public static BlockDecoder bc7() {
        return BC7.INSTANCE;
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
        byte[] dst = new byte[width * height * pixelStride];
        decode(width, height, src, srcPos, dst, 0);
        return dst;
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
     * @param <T>       The type of the output to convert to.
     * @return The newly allocated decoded and converted image.
     * @throws IllegalArgumentException  If the width or height is less than or equal to 0.
     * @throws IndexOutOfBoundsException If the source data is too small.
     */
    public <T> T decode(int width, int height, byte[] src, int srcPos, Converter<T> converter) {
        byte[] decoded = decode(width, height, src, srcPos);
        return converter.convert(width, height, decoded, pixelStride);
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
        decode(src, srcPos, width, height, dst, dstPos, width, height);
    }

    /**
     * Decode an entire image, using an existing byte array as the destination.
     * <p>
     * The destination data must have enough room to store the entire image.
     *
     * @param src       The source data.
     * @param srcPos    The position in the source data.
     * @param srcWidth  The width of the source data.
     * @param srcHeight The height of the source data.
     * @param dst       The destination data.
     * @param dstPos    The position in the destination data.
     * @param dstWidth  The dstWidth of the image.
     * @param dstHeight The dstHeight of the image.
     * @throws IllegalArgumentException  If the dstWidth or dstHeight is less than or equal to 0.
     * @throws IndexOutOfBoundsException If the source or destination data is too small.
     */
    public void decode(byte[] src, int srcPos, int srcWidth, int srcHeight, byte[] dst, int dstPos, int dstWidth, int dstHeight) {
        if (srcWidth <= 0) {
            throw new IllegalArgumentException("srcWidth must be greater than 0");
        }
        if (srcHeight <= 0) {
            throw new IllegalArgumentException("srcHeight must be greater than 0");
        }
        if (dstWidth <= 0 || dstWidth > srcWidth) {
            throw new IllegalArgumentException("dstWidth must be greater than 0 and not greater than srcWidth");
        }
        if (dstHeight <= 0 || dstHeight > srcHeight) {
            throw new IllegalArgumentException("dstHeight must be greater than 0 and not greater than srcHeight");
        }

        int lineStride = dstWidth * pixelStride;
        Objects.checkFromIndexSize(srcPos, byteSize(srcWidth, srcHeight), src.length);
        Objects.checkFromIndexSize(dstPos, dstHeight * lineStride, dst.length);

        for (int y = 0; y < dstHeight; y += BLOCK_HEIGHT) {
            int lineOffset = dstPos + y * lineStride;
            for (int x = 0; x < srcWidth; x += BLOCK_WIDTH, srcPos += bytesPerBlock) {
                if (x >= dstWidth) {
                    continue;
                }
                int dstOffset = lineOffset + x * pixelStride;
                if (y + BLOCK_HEIGHT <= dstHeight && x + BLOCK_WIDTH <= dstWidth) {
                    decodeBlock(src, srcPos, dst, dstOffset, lineStride);
                } else {
                    partialBlock(dstWidth, dstHeight, src, srcPos, dst, dstOffset, x, y, lineStride);
                }
            }
        }
    }

    /**
     * Returns the size in bytes that is required to store an image with the given width and height.
     *
     * @param width  the width of the image
     * @param height the height of the image
     * @return the size in bytes that is required to store an image with the given width and height
     */
    public int byteSize(int width, int height) {
        int widthInBlocks = (width + (BLOCK_WIDTH - 1)) / BLOCK_WIDTH;
        int heightInBlocks = (height + (BLOCK_HEIGHT - 1)) / BLOCK_HEIGHT;
        return widthInBlocks * heightInBlocks * bytesPerBlock;
    }

    private void partialBlock(int width, int height, byte[] src, int srcPos, byte[] dst, int dstPos, int x, int y, int lineStride) {
        int blockStride = BLOCK_WIDTH * pixelStride;
        byte[] block = new byte[BLOCK_HEIGHT * blockStride];
        decodeBlock(src, srcPos, block, 0, blockStride);

        int partialWidth = Math.min(width - x, BLOCK_WIDTH);
        int partialHeight = Math.min(height - y, BLOCK_HEIGHT);
        for (int yy = 0; yy < partialHeight; yy++) {
            System.arraycopy(
                block, yy * blockStride,
                dst, yy * lineStride + dstPos,
                partialWidth * pixelStride
            );
        }
    }
}
