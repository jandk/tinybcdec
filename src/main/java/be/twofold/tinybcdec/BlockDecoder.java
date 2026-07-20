package be.twofold.tinybcdec;

import java.nio.*;

/**
 * This is the main class for decoding block compressed textures.
 * <p>
 * To create a new instance, use one of the static factory methods starting with {@code bc}.
 * <p>
 * To decode an entire image, use the {@link #decode(ByteBuffer, int, int)} or {@link #decode(ByteBuffer, int, int, ByteBuffer)} method.
 * Depending on if you want to allocate a new buffer or use an existing one.
 * <p>
 * Buffers are accessed by absolute index, starting at their current position, so decoding reads and
 * writes without ever advancing a position. Block data is little-endian, so both buffers are switched
 * to {@link ByteOrder#LITTLE_ENDIAN} while decoding and set back to their original order afterwards.
 * <p>
 * <b>Thread safety:</b> instances are not thread-safe. Each thread should use its own decoder instance.
 */
public abstract class BlockDecoder {
    static final int BLOCK_WIDTH = 4;
    static final int BLOCK_HEIGHT = 4;

    final int bytesPerPixel;
    final int bytesPerBlock;
    private ByteBuffer scratch;

    BlockDecoder(int bytesPerPixel, int bytesPerBlock) {
        this.bytesPerPixel = bytesPerPixel;
        this.bytesPerBlock = bytesPerBlock;
    }

    /**
     * Returns a block decoder for BC1.
     *
     * @param opaque Whether to decode the image as opaque or not.
     * @return The block decoder.
     */
    public static BlockDecoder bc1(boolean opaque) {
        return new BC1(opaque ? BC1Mode.OPAQUE : BC1Mode.NORMAL);
    }

    /**
     * Returns a block decoder for BC2.
     *
     * @return The block decoder.
     */
    public static BlockDecoder bc2() {
        return new BC2();
    }

    /**
     * Returns a block decoder for BC3.
     *
     * @return The block decoder.
     */
    public static BlockDecoder bc3() {
        return new BC3();
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
     * @param signed Whether to interpret the data as signed or unsigned.
     * @return The block decoder.
     */
    public static BlockDecoder bc5(boolean signed) {
        return signed ? new BC5S() : new BC5U();
    }

    /**
     * Returns a block decoder for BC6H.
     *
     * @param signed Whether to interpret the data as signed or unsigned.
     * @return The block decoder.
     */
    public static BlockDecoder bc6h(boolean signed) {
        return new BC6H(signed);
    }

    /**
     * Returns a block decoder for BC7.
     *
     * @return The block decoder.
     */
    public static BlockDecoder bc7() {
        return new BC7();
    }

    /**
     * Decodes a single {@value #BLOCK_WIDTH}x{@value #BLOCK_HEIGHT} block.
     * <p>
     * This is the internal hook every format implements. The {@code decode} methods handle validation,
     * byte order, and partial blocks, so implementations may assume both buffers are little-endian and
     * have room for the whole block, and they never bounds check. Positions are left untouched.
     *
     * @param src    The encoded block data.
     * @param srcPos The absolute index of the block in the source.
     * @param dst    The destination for the decoded pixels.
     * @param dstPos The absolute index of the block's top left pixel in the destination.
     * @param stride The number of bytes per line in the destination data.
     */
    abstract void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride);

    /**
     * Decode an entire image, allocating a new buffer as the destination.
     *
     * @param src       The source data.
     * @param srcWidth  The width of the image.
     * @param srcHeight The height of the image.
     * @return The newly allocated decoded image, a heap buffer positioned at 0.
     * @throws IllegalArgumentException  If the width or height is less than or equal to 0,
     *                                   or if the decoded image would not fit in a single buffer.
     * @throws IndexOutOfBoundsException If the source data is too small.
     */
    public ByteBuffer decode(ByteBuffer src, int srcWidth, int srcHeight) {
        ByteBuffer dst = ByteBuffer
            .allocate(decodedByteSize(srcWidth, srcHeight));
        decode(src, srcWidth, srcHeight, dst);
        return dst;
    }

    /**
     * Decode an entire image, using an existing buffer as the destination.
     *
     * @param src       The source data.
     * @param srcWidth  The width of the source image.
     * @param srcHeight The height of the source image.
     * @param dst       The destination data.
     *                  The destination data must have enough room to store the entire image.
     * @throws IllegalArgumentException  If the width or height is less than or equal to 0.
     * @throws IndexOutOfBoundsException If the source or destination data is too small.
     * @throws ReadOnlyBufferException   If the destination is read-only.
     */
    public void decode(ByteBuffer src, int srcWidth, int srcHeight, ByteBuffer dst) {
        decode(src, srcWidth, srcHeight, dst, srcWidth, srcHeight);
    }

    /**
     * Decode an entire image, using an existing buffer as the destination.
     * <p>
     * The destination data must have enough room to store the entire image.
     *
     * @param src       The source data.
     * @param srcWidth  The width of the source data.
     * @param srcHeight The height of the source data.
     * @param dst       The destination data.
     * @param dstWidth  The width of the destination image.
     * @param dstHeight The height of the destination image.
     * @throws IllegalArgumentException  If any width or height is less than or equal to 0.
     * @throws IndexOutOfBoundsException If the destination is larger than the source, in which case
     *                                   there is nothing to fill the remainder with, or if the source
     *                                   or destination data is too small. A destination smaller than
     *                                   the source is fine, and crops to the top left.
     * @throws ReadOnlyBufferException   If the destination is read-only.
     */
    public void decode(ByteBuffer src, int srcWidth, int srcHeight, ByteBuffer dst, int dstWidth, int dstHeight) {
        decode(
            src, 0, 0, srcWidth, srcHeight,
            dst, 0, 0, dstWidth, dstHeight,
            dstWidth, dstHeight
        );
    }

    /**
     * Decodes a region of image data from a source buffer to a destination buffer.
     * Both source and destination buffers must be pre-allocated with sufficient space
     * to contain the required data for the specified regions.
     *
     * @param src       The source buffer containing the encoded image data.
     * @param srcX      The x-coordinate of the source region to decode.
     * @param srcY      The y-coordinate of the source region to decode.
     * @param srcWidth  The width of the source region to decode.
     * @param srcHeight The height of the source region to decode.
     * @param dst       The destination buffer where the decoded image data will be stored.
     * @param dstX      The x-coordinate of the destination region where decoded data will be placed.
     * @param dstY      The y-coordinate of the destination region where decoded data will be placed.
     * @param dstWidth  The width of the destination region.
     * @param dstHeight The height of the destination region.
     * @param width     The target width of the region to decode.
     * @param height    The target height of the region to decode.
     * @throws IllegalArgumentException  If any width or height is less than or equal to 0.
     * @throws IndexOutOfBoundsException If either region falls outside of its image, or if the source
     *                                   or destination buffer is too small for the specified regions.
     * @throws ReadOnlyBufferException   If the destination is read-only.
     */
    public void decode(
        ByteBuffer src, int srcX, int srcY, int srcWidth, int srcHeight,
        ByteBuffer dst, int dstX, int dstY, int dstWidth, int dstHeight,
        int width, int height
    ) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width (" + width + ") or height (" + height + ") is not positive");
        }
        validateRegion("src", srcX, srcY, srcWidth, srcHeight, width, height);
        validateRegion("dst", dstX, dstY, dstWidth, dstHeight, width, height);

        if (src.remaining() < encodedByteSize(srcWidth, srcHeight)) {
            throw new IndexOutOfBoundsException("Not enough data in src buffer");
        }
        if (dst.remaining() < decodedByteSize(dstWidth, dstHeight)) {
            throw new IndexOutOfBoundsException("Not enough data in dst buffer");
        }
        if (dst.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }

        ByteOrder srcOrder = src.order();
        ByteOrder dstOrder = dst.order();
        src.order(ByteOrder.LITTLE_ENDIAN);
        dst.order(ByteOrder.LITTLE_ENDIAN);

        int srcBlocksW = (srcWidth + (BLOCK_WIDTH - 1)) / BLOCK_WIDTH;
        int srcLineStride = srcBlocksW * bytesPerBlock;
        int dstLineStride = dstWidth * bytesPerPixel;

        int srcBase = src.position();
        int dstBase = dst.position();
        for (int y = 0; y < height; ) {
            int srcRowStart = ((srcY + y) / BLOCK_HEIGHT * srcLineStride);
            int dstRowStart = ((dstY + y) * dstLineStride);
            int blockY = (srcY + y) % BLOCK_HEIGHT;
            int blockH = Math.min(BLOCK_HEIGHT - blockY, height - y);

            for (int x = 0; x < width; ) {
                int srcPosStart = srcBase + srcRowStart + ((srcX + x) / BLOCK_WIDTH * bytesPerBlock);
                int dstPosStart = dstBase + dstRowStart + ((dstX + x) * bytesPerPixel);
                int blockX = (srcX + x) % BLOCK_WIDTH;
                int blockW = Math.min(BLOCK_WIDTH - blockX, width - x);

                if (blockX == 0 && x + BLOCK_WIDTH <= width && blockY == 0 && y + BLOCK_HEIGHT <= height) {
                    decodeBlock(src, srcPosStart, dst, dstPosStart, dstLineStride);
                    x += BLOCK_WIDTH;
                } else {
                    partialBlock(
                        src, srcPosStart, dst, dstPosStart, dstLineStride,
                        blockX, blockY, blockW, blockH);
                    x += blockW;
                }
            }
            y += blockH;
        }
        src.order(srcOrder);
        dst.order(dstOrder);
    }

    /**
     * Returns the size in bytes that is required to store an encoded image with the given width and height.
     * <p>
     * This is the size of the block compressed source data, rounded up to whole 4x4 blocks. It is the
     * minimum a source buffer must have remaining to decode an image of this size.
     *
     * @param width  the width of the image
     * @param height the height of the image
     * @return the size in bytes of the encoded image
     * @throws IllegalArgumentException If the width or height is less than or equal to 0,
     *                                  or if the encoded image would not fit in a single buffer.
     * @see #decodedByteSize(int, int)
     */
    public int encodedByteSize(int width, int height) {
        checkDimensions(width, height);
        long widthInBlocks = ((long) width + (BLOCK_WIDTH - 1)) / BLOCK_WIDTH;
        long heightInBlocks = ((long) height + (BLOCK_HEIGHT - 1)) / BLOCK_HEIGHT;
        return toIntSize(widthInBlocks * heightInBlocks * bytesPerBlock, width, height);
    }

    /**
     * Returns the size in bytes that is required to store a decoded image with the given width and height.
     * <p>
     * This is exactly {@code width * height} pixels, with no block rounding, since a partial block only
     * contributes the pixels that fall inside the image. It is the size {@link #decode(ByteBuffer, int, int)}
     * allocates, and the minimum a destination buffer must have remaining.
     *
     * @param width  the width of the image
     * @param height the height of the image
     * @return the size in bytes of the decoded image
     * @throws IllegalArgumentException If the width or height is less than or equal to 0,
     *                                  or if the decoded image would not fit in a single buffer.
     * @see #encodedByteSize(int, int)
     */
    public int decodedByteSize(int width, int height) {
        checkDimensions(width, height);
        return toIntSize((long) width * height * bytesPerPixel, width, height);
    }

    private static void checkDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width (" + width + ") or height (" + height + ") is not positive");
        }
    }

    private static int toIntSize(long size, int width, int height) {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("An image of " + width + " x " + height + " does not fit in a single buffer");
        }
        return (int) size;
    }

    private static void validateRegion(String label, int x, int y, int w, int h, int width, int height) {
        if (x < 0 || y < 0) {
            throw new IndexOutOfBoundsException(label + " x (" + x + ") or y (" + y + ") is negative");
        }
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException(label + " width (" + w + ") or height (" + h + ") is not positive");
        }
        if (width > w - x || height > h - y) {
            throw new IndexOutOfBoundsException(label + " region (" + x + ", " + y + ", " + width + ", " + height + ") is outside of the region (" + w + ", " + h + ")");
        }
    }

    private void partialBlock(
        ByteBuffer src, int srcPos,
        ByteBuffer dst, int dstPos, int lineStride,
        int blockX, int blockY, int blockW, int blockH
    ) {
        if (this.scratch == null) {
            this.scratch = ByteBuffer
                .allocate(BLOCK_WIDTH * BLOCK_HEIGHT * bytesPerPixel);
        }
        ByteBuffer scratch = this.scratch;
        int stride = BLOCK_WIDTH * bytesPerPixel;
        decodeBlock(src, srcPos, scratch, 0, stride);

        int offset = blockY * stride + blockX * bytesPerPixel;
        for (int row = 0; row < blockH; row++) {
            int srcOff = offset + (row * stride);
            int dstOff = dstPos + (row * lineStride);
            ByteIO.copy(scratch, srcOff, dst, dstOff, blockW * bytesPerPixel);
        }
    }
}
