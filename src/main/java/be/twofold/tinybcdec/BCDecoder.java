package be.twofold.tinybcdec;

import java.util.*;

public abstract class BCDecoder {
    static final int BLOCK_WIDTH = 4;
    static final int BLOCK_HEIGHT = 4;

    final BCFormat format;
    final int bytesPerPixel;
    final int redOffset;
    final int greenOffset;
    final int blueOffset;
    final int alphaOffset;

    BCDecoder(BCFormat format, BCOrder order) {
        this.format = format;
        this.bytesPerPixel = order.count() * format.bytesPerValue();
        this.redOffset = order.red() * format.bytesPerValue();
        this.greenOffset = order.green() * format.bytesPerValue();
        this.blueOffset = order.blue() * format.bytesPerValue();
        this.alphaOffset = order.alpha() * format.bytesPerValue();
    }

    public static BCDecoder create(BCFormat format, BCOrder order) {
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

    int rgba(int r, int g, int b, int a) {
        return (r << (redOffset * 8)) | (g << (greenOffset * 8)) | (b << (blueOffset * 8)) | (a << (alphaOffset * 8));
    }

    public abstract void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride);

    public byte[] decode(int width, int height, byte[] src, int srcPos) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0");
        }
        byte[] dst = new byte[width * height * bytesPerPixel];
        decode(width, height, src, srcPos, dst, 0);
        return dst;
    }

    public void decode(int width, int height, byte[] src, int srcPos, byte[] dst, int dstPos) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be greater than 0");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be greater than 0");
        }

        int bytesPerLine = width * bytesPerPixel;
        int widthInBlocks = (width + (BLOCK_WIDTH - 1)) / BLOCK_WIDTH;
        int heightInBlocks = (height + (BLOCK_HEIGHT - 1)) / BLOCK_HEIGHT;
        int expectedSrcLength = widthInBlocks * heightInBlocks * format.bytesPerBlock();
        int expectedDstLength = height * bytesPerLine;
        Objects.checkFromIndexSize(srcPos, expectedSrcLength, src.length);
        Objects.checkFromIndexSize(dstPos, expectedDstLength, dst.length);

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

    private void partialBlock(int width, int height, byte[] src, int srcPos, byte[] dst, int dstPos, int x, int y, int stride) {
        int blockStride = BLOCK_WIDTH * bytesPerPixel;
        byte[] block = new byte[BLOCK_HEIGHT * blockStride];
        decodeBlock(src, srcPos, block, 0, blockStride);
        if (format.minChannels() < 4 && alphaOffset != -1) {
            fillAlpha(block, 0, blockStride);
        }

        int yLimit = Math.min(height - y, BLOCK_HEIGHT);
        int xLimit = Math.min(width - x, BLOCK_WIDTH);
        for (int yy = 0; yy < yLimit; yy++) {
            System.arraycopy(
                block, yy * blockStride,
                dst, dstPos + yy * stride,
                xLimit * bytesPerPixel
            );
        }
    }

    private void fillAlpha(byte[] dst, int dstPos, int stride) {
        if (format == BCFormat.BC6Signed || format == BCFormat.BC6Unsigned) {
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
