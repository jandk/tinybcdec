package be.twofold.tinybcdec;

public enum BCFormat {
    BC1(8, 1, 4),
    BC2(16, 1, 4),
    BC3(16, 1, 4),
    BC4Unsigned(8, 1, 1),
    BC5Unsigned(16, 1, 2),
    BC5UnsignedNormalized(16, 1, 3),
    BC6Unsigned(16, 2, 3),
    BC6Signed(16, 2, 3),
    BC7(16, 1, 4);

    private final int bytesPerBlock;
    private final int bytesPerValue;
    private final int minChannels;

    BCFormat(int bytesPerBlock, int bytesPerValue, int minChannels) {
        this.bytesPerBlock = bytesPerBlock;
        this.bytesPerValue = bytesPerValue;
        this.minChannels = minChannels;
    }

    public int bytesPerBlock() {
        return bytesPerBlock;
    }

    public int bytesPerValue() {
        return bytesPerValue;
    }

    public int minChannels() {
        return minChannels;
    }

    public int size(int width, int height) {
        int widthInBlocks = (width + 3) / 4;
        int heightInBlocks = (height + 3) / 4;
        return widthInBlocks * heightInBlocks * bytesPerBlock;
    }
}
