package be.twofold.tinybcdec;

/**
 * This represents the different block formats that are supported by the decoder.
 *
 * <p>Each block format has a specific number of bytes per block, bytes per value and minimum number of channels.
 * <ul>
 * <li>{@link #getBytesPerBlock()} is the total number of bytes that are required to store a single block of data.</li>
 * </ul>
 */
public enum BlockFormat {
    /**
     * BC1: 8 bytes per block, 1 byte per value, 4 channels (RGBA)
     */
    BC1(8),
    /**
     * BC1: 8 bytes per block, 1 byte per value, 4 channels (RGB_)
     */
    BC1_NO_ALPHA(8),
    /**
     * BC2: 16 bytes per block, 1 byte per value, 4 channels (RGBA)
     */
    BC2(16),
    /**
     * BC3: 16 bytes per block, 1 byte per value, 4 channels (RGBA)
     */
    BC3(16),
    /**
     * BC4 Unsigned: 8 bytes per block, 1 byte per value, 1 channel (R)
     */
    BC4U(8),
    /**
     * BC4 Signed: 8 bytes per block, 1 byte per value, 1 channel (R)
     */
    BC4S(8),
    /**
     * BC5 Unsigned: 16 bytes per block, 1 byte per value, 2 channels (RG)
     */
    BC5U(16),
    /**
     * BC5 Unsigned Reconstruct Z: 16 bytes per block, 1 byte per value, 3 channels (RGB)
     */
    BC5U_RECONSTRUCT_Z(16),
    /**
     * BC5 Signed: 16 bytes per block, 1 byte per value, 2 channels (RG)
     */
    BC5S(16),
    /**
     * BC5 Signed Reconstruct Z: 16 bytes per block, 1 byte per value, 3 channels (RGB)
     */
    BC5S_RECONSTRUCT_Z(16),
    /**
     * BC6 Unsigned: 16 bytes per block, 2 bytes per value, 3 channels (RGB)
     */
    BC6HU(16),
    /**
     * BC6 Signed: 16 bytes per block, 2 bytes per value, 3 channels (RGB)
     */
    BC6HS(16),
    /**
     * BC7: 16 bytes per block, 1 byte per value, 4 channels (RGBA)
     */
    BC7(16);

    private static final int BLOCK_WIDTH = BlockDecoder.BLOCK_WIDTH;
    private static final int BLOCK_HEIGHT = BlockDecoder.BLOCK_HEIGHT;

    private final int bytesPerBlock;

    BlockFormat(int bytesPerBlock) {
        this.bytesPerBlock = bytesPerBlock;
    }

    /**
     * Returns the total number of bytes that are required to store a single block of data.
     *
     * @return the total number of bytes that are required to store a single block of data
     */
    public int getBytesPerBlock() {
        return bytesPerBlock;
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
