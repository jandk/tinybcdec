package be.twofold.tinybcdec;

import java.nio.*;

final class BC6HFloat extends BlockDecoder {
    static final int BPP = 16;

    private static final int TILE_STRIDE = BLOCK_WIDTH * BC6H.BPP;

    private final BC6H decoder;
    private final ByteBuffer tile = ByteBuffer
        .allocate(BLOCK_HEIGHT * TILE_STRIDE)
        .order(ByteOrder.LITTLE_ENDIAN);

    BC6HFloat(boolean signed) {
        super(BPP, 16);
        this.decoder = new BC6H(signed);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        ByteBuffer tile = this.tile;
        decoder.decodeBlock(src, srcPos, tile, 0, TILE_STRIDE);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                long rgba = ByteIO.getLong(tile, y * TILE_STRIDE + x * BC6H.BPP);
                int offset = dstPos + x * BPP;
                ByteIO.setFloat(dst, offset /**/, Platform.float16ToFloat((short) (rgba /*  */)));
                ByteIO.setFloat(dst, offset + +4, Platform.float16ToFloat((short) (rgba >>> 16)));
                ByteIO.setFloat(dst, offset + +8, Platform.float16ToFloat((short) (rgba >>> 32)));
                ByteIO.setFloat(dst, offset + 12, Platform.float16ToFloat((short) (rgba >>> 48)));
            }
            dstPos += stride;
        }
    }
}
