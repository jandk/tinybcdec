package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BlockDecoderTest {

    @Test
    void testPartialBlock() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");

        byte[] actual = BlockDecoder.bc4(false)
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
        var decoder = BlockDecoder.bc4(false);
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
    void testPartialBlockCropExtra() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");
        byte[] expected = BCTestUtils.readPng("/bc4u-part.png");

        int srcWidth = 157;
        int srcHeight = 119;
        int dstOffset = 31;

        byte[] dst = new byte[8 * 8 + dstOffset];
        var decoder = BlockDecoder.bc4(false);

        // Test all offsets between 0 and 8
        for (int srcY = 1; srcY < 8; srcY++) {
            for (int srcX = 1; srcX < 8; srcX++) {
                int w = 8;
                int h = 8;
                decoder.decode(
                    src, BCTestUtils.DDS_HEADER_SIZE, srcX, srcY, srcWidth, srcHeight,
                    dst, dstOffset, 0, 0, w, h,
                    w, h
                );

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        assertThat(dst[y * w + x + dstOffset]).isEqualTo(expected[(srcY + y) * srcWidth + (srcX + x)]);
                    }
                }
            }
        }
    }

    @Test
    void testValidation() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.bc1(false)
                .decode(null, 0, 0, 256))
            .withMessage("src width (0) or height (256) is not positive");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.bc1(false)
                .decode(null, 0, 256, 0))
            .withMessage("src width (256) or height (0) is not positive");
    }

}
