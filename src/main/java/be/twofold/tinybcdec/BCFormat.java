package be.twofold.tinybcdec;

public enum BCFormat {
    BC1(8, 4),
    BC2(16, 4),
    BC3(16, 4),
    BC4U(8, 1),
    BC5U(16, 2),
    BC5U_BLUE(16, 2),
    BC6H_UF16(16, 6),
    BC6H_SF16(16, 6),
    BC7(16, 4);

    private final int bytesPerBlock;
    private final int minBytesPerPixel;

    BCFormat(int bytesPerBlock, int minBytesPerPixel) {
        this.bytesPerBlock = bytesPerBlock;
        this.minBytesPerPixel = minBytesPerPixel;
    }

    public int bytesPerBlock() {
        return bytesPerBlock;
    }

    public int minBytesPerPixel() {
        return minBytesPerPixel;
    }

    public int size(int width, int height) {
        int widthInBlocks = (width + 3) / 4;
        int heightInBlocks = (height + 3) / 4;
        return widthInBlocks * heightInBlocks * bytesPerBlock;
    }
}
