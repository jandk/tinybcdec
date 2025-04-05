package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BlockDecoderTest {

    @Test
    void testPartialBlock() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC4U)
            .decode(157, 119, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc4u-part.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testPartialBlockCrop() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");
        byte[] expected = BCTestUtils.readPng("/bc4u-part.png");

        // Add a bit of chaos for everything
        int srcWidth = 157;
        int srcHeight = 119;
        int dstOffset = 31;

        byte[] dst = new byte[8 * 8 + dstOffset];
        var decoder = BlockDecoder.create(BlockFormat.BC4U);
        for (int h = 1; h <= 8; h++) {
            for (int w = 1; w <= 8; w++) {
                decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, srcWidth, srcHeight, dst, dstOffset, w, h);

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        assertThat(dst[y * w + x + dstOffset]).isEqualTo(expected[y * srcWidth + x]);
                    }
                }
            }
        }
    }

    @Test
    void testValidation() {
        assertThatNullPointerException()
            .isThrownBy(() -> BlockDecoder.create(null));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1)
                .decode(0, 256, null, 0))
            .withMessage("srcWidth must be greater than 0");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1)
                .decode(256, 0, null, 0))
            .withMessage("srcHeight must be greater than 0");
    }

}
