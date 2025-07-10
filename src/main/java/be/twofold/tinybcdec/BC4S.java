package be.twofold.tinybcdec;

import be.twofold.tinybcdec.utils.*;

final class BC4S extends BlockDecoder {
    BC4S(int pixelStride) {
        super(pixelStride, 8);
    }

    @Override
    public void decodeBlock(byte[] src, int srcPos, byte[] dst, int dstPos, int stride) {
        long block = ByteArrays.getLong(src, srcPos);

        // @formatter:off
        int a0 = Math.max(-127, (byte)  block       );
        int a1 = Math.max(-127, (byte) (block >>> 8));

        byte[] alphas = {scale127(a0), scale127(a1), 0, 0, 0, 0, 0, (byte) 0xFF};
        if (a0 > a1) {
            alphas[2] = scale889(6 * a0 +     a1);
            alphas[3] = scale889(5 * a0 + 2 * a1);
            alphas[4] = scale889(4 * a0 + 3 * a1);
            alphas[5] = scale889(3 * a0 + 4 * a1);
            alphas[6] = scale889(2 * a0 + 5 * a1);
            alphas[7] = scale889(    a0 + 6 * a1);
        } else {
            alphas[2] = scale635(4 * a0 +     a1);
            alphas[3] = scale635(3 * a0 + 2 * a1);
            alphas[4] = scale635(2 * a0 + 3 * a1);
            alphas[5] = scale635(    a0 + 4 * a1);
        }
        // @formatter:on

        long indices = block >>> 16;
        for (int y = 0; y < BLOCK_HEIGHT; y++) {
            for (int x = 0; x < BLOCK_WIDTH; x++) {
                byte alpha = alphas[(int) (indices & 0x07)];
                dst[dstPos + x * pixelStride] = alpha;
                indices >>>= 3;
            }
            dstPos += stride;
        }
    }

    private static byte scale127(int i) {
        return (byte) ((i * 129 + 16384) >>> 7);
    }

    private static byte scale889(int i) {
        return (byte) ((i * 75193 + 67108864) >>> 19);
    }

    private static byte scale635(int i) {
        return (byte) ((i * 13159 + 8388708) >>> 16);
    }
}
