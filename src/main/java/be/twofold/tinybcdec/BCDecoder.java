package be.twofold.tinybcdec;

import java.util.*;

public abstract class BCDecoder {
    static final int BLOCK_WIDTH = 4;
    static final int BLOCK_HEIGHT = 4;

    final int bytesPerBlock;
    final int bytesPerPixel;
    final int rOffset;
    final int gOffset;
    final int bOffset;
    final int aOffset;

    BCDecoder(int bytesPerBlock, int bytesPerPixel, int rOffset, int gOffset, int bOffset, int aOffset) {
        this.bytesPerBlock = bytesPerBlock;
        this.bytesPerPixel = bytesPerPixel;
        this.rOffset = rOffset;
        this.gOffset = gOffset;
        this.bOffset = bOffset;
        this.aOffset = aOffset;
    }

    /**
     * Creates a new {@link Builder} for creating a {@link BCDecoder}.
     *
     * @param format The format of the block compressed data.
     */
    public static Builder builder(BCFormat format) {
        return new Builder(format);
    }

    int rgb(int r, int g, int b) {
        return rgba(r, g, b, 255);
    }

    int rgba(int r, int g, int b, int a) {
        return (r << (rOffset * 8)) | (g << (gOffset * 8)) | (b << (bOffset * 8)) | (a << (aOffset * 8));
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

        int stride = width * bytesPerPixel;
        int widthInBlocks = (width + (BLOCK_WIDTH - 1)) / BLOCK_WIDTH;
        int heightInBlocks = (height + (BLOCK_HEIGHT - 1)) / BLOCK_HEIGHT;
        int expectedSrcLength = widthInBlocks * heightInBlocks * bytesPerBlock;
        int expectedDstLength = height * stride;
        Objects.checkFromIndexSize(srcPos, expectedSrcLength, src.length);
        Objects.checkFromIndexSize(dstPos, expectedDstLength, dst.length);

        for (int y = 0; y < height; y += BLOCK_HEIGHT) {
            for (int x = 0; x < width; x += BLOCK_WIDTH, srcPos += bytesPerBlock) {
                if (height - y >= BLOCK_HEIGHT && width - x >= BLOCK_WIDTH) {
                    decodeBlock(src, srcPos, dst, dstPos + y * stride + x * bytesPerPixel, stride);
                    continue;
                }

                // Partial block
                byte[] block = new byte[BLOCK_WIDTH * BLOCK_HEIGHT * bytesPerBlock];
                decodeBlock(src, srcPos, block, 0, 4 * bytesPerPixel);

                for (int yy = 0; yy < BLOCK_HEIGHT && y + yy < height; yy++) {
                    for (int xx = 0; xx < BLOCK_WIDTH && x + xx < width; xx++) {
                        System.arraycopy(
                            block, (yy * BLOCK_HEIGHT + xx) * bytesPerPixel,
                            dst, dstPos + (y + yy) * stride + (x + xx) * bytesPerPixel,
                            bytesPerPixel);
                    }
                }
            }
        }
    }

    public static final class Builder {
        private final BCFormat format;
        private int bytesPerPixel;
        private int redChannel = -1;
        private int greenChannel = -1;
        private int blueChannel = -1;
        private int alphaChannel = -1;

        private Builder(BCFormat format) {
            this.format = format;
            this.bytesPerPixel = format.minBytesPerPixel();
        }

        public Builder bytesPerPixel(int bytesPerPixel) {
            if (bytesPerPixel < format.minBytesPerPixel()) {
                throw new IllegalArgumentException("bytesPerPixel must be at least " + format.minBytesPerPixel());
            }
            if ((format == BCFormat.BC6H_UF16 || format == BCFormat.BC6H_SF16) && bytesPerPixel % 2 != 0) {
                throw new IllegalArgumentException("bytesPerPixel must be a multiple of 2 for BC6H");
            }
            this.bytesPerPixel = bytesPerPixel;

            // Verify that the channels are still in range
            return this
                .redChannel(redChannel)
                .greenChannel(greenChannel)
                .blueChannel(blueChannel)
                .alphaChannel(alphaChannel);
        }

        /**
         * Sets the red channel index. Must be in the range [0, bytesPerPixel).
         *
         * @param redChannel The red channel index.
         */
        public Builder redChannel(int redChannel) {
            if (redChannel != -1 && (redChannel < 0 || redChannel >= bytesPerPixel)) {
                throw new IllegalArgumentException("redChannel must be in the range [0, bytesPerPixel)");
            }
            this.redChannel = redChannel;
            return this;
        }

        /**
         * Sets the green channel index. Must be in the range [0, bytesPerPixel).
         *
         * @param greenChannel The green channel index.
         */
        public Builder greenChannel(int greenChannel) {
            if (greenChannel != -1 && (greenChannel < 0 || greenChannel >= bytesPerPixel)) {
                throw new IllegalArgumentException("greenChannel must be in the range [0, bytesPerPixel)");
            }
            this.greenChannel = greenChannel;
            return this;
        }

        /**
         * Sets the blue channel index. Must be in the range [0, bytesPerPixel).
         *
         * @param blueChannel The blue channel index.
         */
        public Builder blueChannel(int blueChannel) {
            if (blueChannel != -1 && (blueChannel < 0 || blueChannel >= bytesPerPixel)) {
                throw new IllegalArgumentException("blueChannel must be in the range [0, bytesPerPixel)");
            }
            this.blueChannel = blueChannel;
            return this;
        }

        /**
         * Sets the alpha channel index. Must be in the range [0, bytesPerPixel).
         *
         * @param alphaChannel The alpha channel index.
         */
        public Builder alphaChannel(int alphaChannel) {
            if (alphaChannel != -1 && (alphaChannel < 0 || alphaChannel >= bytesPerPixel)) {
                throw new IllegalArgumentException("alphaChannel must be in the range [0, bytesPerPixel)");
            }
            this.alphaChannel = alphaChannel;
            return this;
        }

        public Builder orderR() {
            return this
                .bytesPerPixel(1)
                .redChannel(0);
        }

        public Builder orderRGB() {
            return this
                .bytesPerPixel(3)
                .redChannel(0)
                .greenChannel(1)
                .blueChannel(2);
        }

        public Builder orderBGR() {
            return this
                .bytesPerPixel(3)
                .redChannel(2)
                .greenChannel(1)
                .blueChannel(0);
        }

        /**
         * RGBA order. Useful for exporting to PNG, etc.
         */
        public Builder orderRGBA() {
            return this
                .redChannel(0)
                .greenChannel(1)
                .blueChannel(2)
                .alphaChannel(3);
        }

        /**
         * ABGR order. Useful for Java2D.
         */
        public Builder orderABGR() {
            return this
                .redChannel(3)
                .greenChannel(2)
                .blueChannel(1)
                .alphaChannel(0);
        }

        /**
         * BGRA order. Useful for JavaFX.
         */
        public Builder orderBGRA() {
            return this
                .redChannel(2)
                .greenChannel(1)
                .blueChannel(0)
                .alphaChannel(3);
        }

        public BCDecoder build() {
            if (bytesPerPixel >= 1 && redChannel == -1) {
                throw new IllegalArgumentException("redChannel must be set for at least 1 byte per pixel");
            }
            if (bytesPerPixel >= 2 && greenChannel == -1) {
                throw new IllegalArgumentException("greenChannel must be set for at least 2 bytes per pixel");
            }
            if (bytesPerPixel >= 3 && blueChannel == -1) {
                throw new IllegalArgumentException("blueChannel must be set for at least 3 bytes per pixel");
            }
            if (bytesPerPixel >= 4 && alphaChannel == -1) {
                throw new IllegalArgumentException("alphaChannel must be set for at least 4 bytes per pixel");
            }
            switch (format) {
                case BC1:
                    return new BC1Decoder(bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel, false);
                case BC2:
                    return new BC2Decoder(bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel);
                case BC3:
                    return new BC3Decoder(bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel);
                case BC4U:
                    return new BC4UDecoder(bytesPerPixel, redChannel);
                case BC5U:
                    return new BC5UDecoder(bytesPerPixel, redChannel, greenChannel);
                case BC5U_BLUE:
                    return new BC5UDecoder(bytesPerPixel, redChannel, greenChannel, blueChannel);
                case BC6H_UF16:
                    return new BC6HDecoder(bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel, false);
                case BC6H_SF16:
                    return new BC6HDecoder(bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel, true);
                case BC7:
                    return new BC7Decoder(bytesPerPixel, redChannel, greenChannel, blueChannel, alphaChannel);
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }
        }
    }
}
