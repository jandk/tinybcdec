package be.twofold.tinybcdec;

import java.util.*;

public final class BCDecoder {
    private final BCFormat format;
    private final BlockDecoder decoder;
    private final int bytesPerPixel;
    private final int redOffset;
    private final int greenOffset;
    private final int blueOffset;
    private final int alphaOffset;
    final byte[] buffer = new byte[128];

    public BCDecoder(BCFormat format, int bytesPerPixel, int redOffset, int greenOffset, int blueOffset, int alphaOffset) {
        Objects.requireNonNull(format, "format must not be null");
        if (bytesPerPixel < format.minBytesPerPixel()) {
            throw new IllegalArgumentException("bytesPerPixel must be at least " + format.minBytesPerPixel());
        }
        if (redOffset != -1 && redOffset < 0 || redOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("redOffset must be in the range [0, bytesPerPixel)");
        }
        if (greenOffset != -1 && greenOffset < 0 || greenOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("greenOffset must be in the range [0, bytesPerPixel)");
        }
        if (blueOffset != -1 && blueOffset < 0 || blueOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("blueOffset must be in the range [0, bytesPerPixel)");
        }
        if (alphaOffset != -1 && alphaOffset < 0 || alphaOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("alphaOffset must be in the range [0, bytesPerPixel)");
        }

        this.format = format;
        this.decoder = createDecoder(format);
        this.bytesPerPixel = bytesPerPixel;
        this.redOffset = redOffset;
        this.greenOffset = greenOffset;
        this.blueOffset = blueOffset;
        this.alphaOffset = alphaOffset;
    }

    private BlockDecoder createDecoder(BCFormat format) {
        switch (format) {
            case BC1:
                return new BC1Decoder(bytesPerPixel, redOffset, greenOffset, blueOffset, alphaOffset, false);
            case BC2:
                return new BC2Decoder(bytesPerPixel, redOffset, greenOffset, blueOffset, alphaOffset);
            case BC3:
                return new BC3Decoder(bytesPerPixel, redOffset, greenOffset, blueOffset, alphaOffset);
            case BC4U:
                return new BC4UDecoder(bytesPerPixel, redOffset);
            case BC5U:
                return new BC5UDecoder(bytesPerPixel, redOffset, greenOffset);
            case BC5U_BLUE:
                return new BC5UDecoder(bytesPerPixel, redOffset, greenOffset, blueOffset);
            case BC6H_UF16:
                return new BC6HDecoder(bytesPerPixel, redOffset, greenOffset, blueOffset, alphaOffset, false);
            case BC6H_SF16:
                return new BC6HDecoder(bytesPerPixel, redOffset, greenOffset, blueOffset, alphaOffset, true);
            case BC7:
                return new BC7Decoder(bytesPerPixel, redOffset, greenOffset, blueOffset, alphaOffset);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

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

        int stride = width * bytesPerPixel;
        int widthInBlocks = (width + 3) / 4;
        int heightInBlocks = (height + 3) / 4;
        int expectedSrcLength = widthInBlocks * heightInBlocks * format.bytesPerBlock();
        int expectedDstLength = height * stride;
        Objects.checkFromIndexSize(srcPos, expectedSrcLength, src.length);
        Objects.checkFromIndexSize(dstPos, expectedDstLength, dst.length);

        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4, srcPos += format.bytesPerBlock()) {
                decoder.decodeBlock(src, srcPos, buffer, 0, stride);

                if (decoder instanceof BC6HDecoder) {
                    throw new UnsupportedOperationException("BC6HDecoder not supported");
                } else {
                    copyBlock(dst, dstPos + y * stride + x * bytesPerPixel, stride);
                }
            }
        }
    }

    private void copyBlock(byte[] dst, int dstPos, int stride) {
        for (int y = 0, bufferPos = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++, bufferPos += 4) {
                int color = ByteArrays.getInt(buffer, bufferPos);
                if (redOffset != -1) {
                    dst[dstPos + redOffset] = (byte) ((color >>> 0) & 0xff);
                }
                if (greenOffset != -1) {
                    dst[dstPos + greenOffset] = (byte) ((color >>> 8) & 0xff);
                }
                if (blueOffset != -1) {
                    dst[dstPos + blueOffset] = (byte) ((color >>> 16) & 0xff);
                }
                if (alphaOffset != -1) {
                    dst[dstPos + alphaOffset] = (byte) ((color >>> 24) & 0xff);
                }
                dstPos += bytesPerPixel;
            }
            dstPos += stride - 4 * bytesPerPixel;
        }
    }
}
