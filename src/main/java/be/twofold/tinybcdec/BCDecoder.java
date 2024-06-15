package be.twofold.tinybcdec;

import java.util.*;

public abstract class BCDecoder {
    final int bytesPerBlock;
    final int bytesPerPixel;
    final int rOffset;
    final int gOffset;
    final int bOffset;
    final int aOffset;

    public BCDecoder(int bytesPerBlock, int minBytesPerPixel, int bytesPerPixel, int rOffset, int gOffset, int bOffset, int aOffset) {
        if (bytesPerPixel < minBytesPerPixel) {
            throw new IllegalArgumentException("bytesPerPixel must be at least " + minBytesPerPixel);
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
        this.bytesPerBlock = bytesPerBlock;
        this.bytesPerPixel = bytesPerPixel;
        this.rOffset = rOffset;
        this.gOffset = gOffset;
        this.bOffset = bOffset;
        this.aOffset = aOffset;
    }

    int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    int rgba(int r, int g, int b, int a) {
        return (r << (rOffset * 8)) | (g << (gOffset * 8)) | (b << (bOffset * 8)) | (a << (aOffset * 8));
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
        int expectedSrcLength = widthInBlocks * heightInBlocks * bytesPerBlock;
        int expectedDstLength = height * stride;
        Objects.checkFromIndexSize(srcPos, expectedSrcLength, src.length);
        Objects.checkFromIndexSize(dstPos, expectedDstLength, dst.length);

        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4, srcPos += bytesPerBlock) {
                decodeBlock(src, srcPos, dst, dstPos + y * stride + x * bytesPerPixel, stride);
            }
        }
    }

    public abstract void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride);
}
