package be.twofold.tinybcdec;

import java.nio.*;

final class BC7Float extends BlockDecoder {
    static final int BPP = 16;

    private static final int TILE_STRIDE = BLOCK_WIDTH * BC7.BPP;
    private static final float[] LUT = new float[256];

    static {
        for (int i = 0; i < LUT.length; i++) {
            LUT[i] = i / 255.0f;
        }
    }

    private final BC7 decoder = new BC7();
    private final ByteBuffer tile = ByteBuffer
        .allocate(BLOCK_WIDTH * BLOCK_HEIGHT * BC7.BPP)
        .order(ByteOrder.LITTLE_ENDIAN);

    BC7Float() {
        super(BPP, 16);
    }

    @Override
    void decodeBlock(ByteBuffer src, int srcPos, ByteBuffer dst, int dstPos, int stride) {
        ByteBuffer tile = this.tile;
        decoder.decodeBlock(src, srcPos, tile, 0, TILE_STRIDE);

        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                int bgra = ByteIO.getInt(tile, y * TILE_STRIDE + x * BC7.BPP);
                int offset = dstPos + x * BPP;
                ByteIO.setFloat(dst, offset /**/, LUT[(bgra >>> 16) & 0xFF]);
                ByteIO.setFloat(dst, offset + +4, LUT[(bgra >>> +8) & 0xFF]);
                ByteIO.setFloat(dst, offset + +8, LUT[(bgra /*  */) & 0xFF]);
                ByteIO.setFloat(dst, offset + 12, LUT[(bgra >>> 24) /*  */]);
            }
            dstPos += stride;
        }
    }
}
