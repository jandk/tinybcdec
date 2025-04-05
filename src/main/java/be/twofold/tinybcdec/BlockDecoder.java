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
    static final int BLOCK_WIDTH = 4;
    static final int BLOCK_HEIGHT = 4;

    private final BlockFormat format;
    final int pixelStride;

    BlockDecoder(BlockFormat format, int pixelStride) {
        this.format = format;
        this.pixelStride = pixelStride;
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
                return new BC1Decoder(BC1Decoder.Mode.NORMAL);
            case BC1_NO_ALPHA:
                return new BC1Decoder(BC1Decoder.Mode.OPAQUE);
            case BC2:
                return new BC2Decoder();
            case BC3:
                return new BC3Decoder();
            case BC4U:
                return new BC4UDecoder(1);
            case BC4S:
                return new BC4SDecoder(1);
            case BC5U:
                return new BC5UDecoder(false);
            case BC5U_RECONSTRUCT_Z:
                return new BC5UDecoder(true);
            case BC5S:
                return new BC5SDecoder(false);
            case BC5S_RECONSTRUCT_Z:
                return new BC5SDecoder(true);
            case BC6H_UF16:
                return new BC6HDecoder(false, false);
            case BC6H_SF16:
                return new BC6HDecoder(true, false);
            case BC6H_UF32:
                return new BC6HDecoder(false, true);
            case BC6H_SF32:
                return new BC6HDecoder(true, true);
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
        Objects.checkFromIndexSize(srcPos, format.size(srcWidth, srcHeight), src.length);
        Objects.checkFromIndexSize(dstPos, dstHeight * lineStride, dst.length);

        int bpb = format.getBytesPerBlock();
        for (int y = 0; y < dstHeight; y += BLOCK_HEIGHT) {
            int lineOffset = dstPos + y * lineStride;
            for (int x = 0; x < srcWidth; x += BLOCK_WIDTH, srcPos += bpb) {
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
