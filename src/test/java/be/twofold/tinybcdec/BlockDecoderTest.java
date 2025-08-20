package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BlockDecoderTest {

    @Test
    void testPartialBlock() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");

        byte[] actual = BlockDecoder.bc4(BlockDecoder.Signedness.UNSIGNED)
            .decode(src, BCTestUtils.DDS_HEADER_SIZE, 157, 119);
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
        var decoder = BlockDecoder.bc4(BlockDecoder.Signedness.UNSIGNED);
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
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.bc1(BlockDecoder.Opacity.OPAQUE)
                .decode(null, 0, 0, 256))
            .withMessage("srcWidth must be greater than 0");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.bc1(BlockDecoder.Opacity.OPAQUE)
                .decode(null, 0, 256, 0))
            .withMessage("srcHeight must be greater than 0");
    }

}
