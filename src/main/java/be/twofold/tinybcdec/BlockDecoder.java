package be.twofold.tinybcdec;

import java.util.*;

public final class BlockDecoder {
    private final BCFormat format;
    private final BCDecoder decoder;
    private final int bytesPerPixel;
    private final int rOffset;
    private final int gOffset;
    private final int bOffset;
    private final int aOffset;
    final byte[] buffer = new byte[128];

    public BlockDecoder(BCFormat format, int bytesPerPixel, int rOffset, int gOffset, int bOffset, int aOffset) {
        Objects.requireNonNull(format, "format must not be null");
        if (bytesPerPixel < format.minBytesPerPixel()) {
            throw new IllegalArgumentException("bytesPerPixel must be at least " + format.minBytesPerPixel());
        }
        if (rOffset != -1 && rOffset < 0 || rOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("rOffset must be in the range [0, bytesPerPixel)");
        }
        if (gOffset != -1 && gOffset < 0 || gOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("gOffset must be in the range [0, bytesPerPixel)");
        }
        if (bOffset != -1 && bOffset < 0 || bOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("bOffset must be in the range [0, bytesPerPixel)");
        }
        if (aOffset != -1 && aOffset < 0 || aOffset >= bytesPerPixel) {
            throw new IllegalArgumentException("aOffset must be in the range [0, bytesPerPixel)");
        }

        this.format = format;
        this.decoder = createDecoder(format);
        this.bytesPerPixel = bytesPerPixel;
        this.rOffset = rOffset;
        this.gOffset = gOffset;
        this.bOffset = bOffset;
        this.aOffset = aOffset;
    }

    private BCDecoder createDecoder(BCFormat format) {
        switch (format) {
            case BC1:
                return new BC1Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset, false);
            case BC2:
                return new BC2Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset);
            case BC3:
                return new BC3Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset);
            case BC4U:
                return new BC4UDecoder(bytesPerPixel, rOffset);
            case BC5U:
                return new BC5UDecoder(bytesPerPixel, rOffset, gOffset);
            case BC5U_BLUE:
                return new BC5UDecoder(bytesPerPixel, rOffset, gOffset, bOffset);
            case BC6H_UF16:
                return new BC6HDecoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset, false);
            case BC6H_SF16:
                return new BC6HDecoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset, true);
            case BC7:
                return new BC7Decoder(bytesPerPixel, rOffset, gOffset, bOffset, aOffset);
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
                if (rOffset != -1) {
                    dst[dstPos + rOffset] = (byte) ((color >>> 0) & 0xff);
                }
                if (gOffset != -1) {
                    dst[dstPos + gOffset] = (byte) ((color >>> 8) & 0xff);
                }
                if (bOffset != -1) {
                    dst[dstPos + bOffset] = (byte) ((color >>> 16) & 0xff);
                }
                if (aOffset != -1) {
                    dst[dstPos + aOffset] = (byte) ((color >>> 24) & 0xff);
                }
                dstPos += bytesPerPixel;
            }
            dstPos += stride - 4 * bytesPerPixel;
        }
    }
}
