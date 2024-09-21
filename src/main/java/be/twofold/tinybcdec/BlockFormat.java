package be.twofold.tinybcdec;

/**
 * This represents the different block formats that are supported by the decoder.
 *
 * <p>Each block format has a specific number of bytes per block, bytes per value and minimum number of channels.
 * <ul>
 * <li>{@link #bytesPerBlock()} is the total number of bytes that are required to store a single block of data.</li>
 * <li>{@link #bytesPerValue()} is the number of bytes that are required to store a single pixel channel.</li>
 * <li>{@link #minChannels()} is the minimum number of channels that are required to store a single pixel.</li>
 * </ul>
 */
public enum BlockFormat {
    /**
     * BC1: 8 bytes per block, 1 byte per value, 4 channels (RGBA)
     */
    BC1(8, 1, 4),
    /**
     * BC2: 16 bytes per block, 1 byte per value, 4 channels (RGBA)
     */
    BC2(16, 1, 4),
    /**
     * BC3: 16 bytes per block, 1 byte per value, 4 channels (RGBA)
     */
    BC3(16, 1, 4),
    /**
     * BC4 Unsigned: 8 bytes per block, 1 byte per value, 1 channel (R)
     */
    BC4Unsigned(8, 1, 1),
    /**
     * BC4 Signed: 8 bytes per block, 1 byte per value, 1 channel (R)
     */
    BC4Signed(8, 1, 1),
    /**
     * BC5 Unsigned: 16 bytes per block, 1 byte per value, 2 channels (RG)
     */
    BC5Unsigned(16, 1, 2),
    /**
     * BC5 Unsigned Normalized: 16 bytes per block, 1 byte per value, 3 channels (RGB)
     */
    BC5UnsignedNormalized(16, 1, 3),
    /**
     * BC5 Signed: 16 bytes per block, 1 byte per value, 2 channels (RG)
     */
    BC5Signed(16, 1, 2),
    /**
     * BC5 Signed Normalized: 16 bytes per block, 1 byte per value, 3 channels (RGB)
     */
    BC5SignedNormalized(16, 1, 3),
    /**
     * BC6 Unsigned: 16 bytes per block, 2 bytes per value, 3 channels (RGB)
     */
    BC6Unsigned(16, 2, 3),
    /**
     * BC6 Signed: 16 bytes per block, 2 bytes per value, 3 channels (RGB)
     */
    BC6Signed(16, 2, 3),
    /**
     * BC7: 16 bytes per block, 1 byte per value, 4 channels (RGBA)
     */
    BC7(16, 1, 4);

    private static final int BLOCK_WIDTH = 4;
    private static final int BLOCK_HEIGHT = 4;

    private final int bytesPerBlock;
    private final int bytesPerValue;
    private final int minChannels;

    BlockFormat(int bytesPerBlock, int bytesPerValue, int minChannels) {
        this.bytesPerBlock = bytesPerBlock;
        this.bytesPerValue = bytesPerValue;
        this.minChannels = minChannels;
    }

    /**
     * Returns the total number of bytes that are required to store a single block of data.
     *
     * @return the total number of bytes that are required to store a single block of data
     */
    public int bytesPerBlock() {
        return bytesPerBlock;
    }

    /**
     * Returns the number of bytes that are required to store a single pixel channel.
     *
     * @return the number of bytes that are required to store a single pixel channel
     */
    public int bytesPerValue() {
        return bytesPerValue;
    }

    /**
     * Returns the minimum number of channels that are required to store a single pixel.
     *
     * @return the minimum number of channels that are required to store a single pixel
     */
    public int minChannels() {
        return minChannels;
    }

    /**
     * Returns the size in bytes that is required to store an image with the given width and height.
     *
     * @param width  the width of the image
     * @param height the height of the image
     * @return the size in bytes that is required to store an image with the given width and height
     */
    public int size(int width, int height) {
        int widthInBlocks = (width + (BLOCK_WIDTH - 1)) / BLOCK_WIDTH;
        int heightInBlocks = (height + (BLOCK_HEIGHT - 1)) / BLOCK_HEIGHT;
        return widthInBlocks * heightInBlocks * bytesPerBlock;
    }
}
