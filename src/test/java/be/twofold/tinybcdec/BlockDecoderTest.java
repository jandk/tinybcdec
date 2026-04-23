package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BlockDecoderTest {

    @Test
    void testPartialBlock() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc4u-part.dds");

        ByteBuffer actual = BlockDecoder.bc4(false)
            .decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 157, 119);
        ByteBuffer expected = BCTestUtils.readPng("/bc4u-part.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testPartialBlockCrop() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc4u-part.dds");
        ByteBuffer expected = BCTestUtils.readPng("/bc4u-part.png");

        // Add a bit of chaos for everything
        int srcWidth = 157;
        int srcHeight = 119;
        int dstOffset = 31;

        ByteBuffer dst = ByteBuffer.allocate(8 * 8 + dstOffset);
        BlockDecoder decoder = BlockDecoder.bc4(false);
        for (int h = 1; h <= 8; h++) {
            for (int w = 1; w <= 8; w++) {
                decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), srcWidth, srcHeight, dst.position(dstOffset), w, h);

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        assertThat(dst.get(y * w + x + dstOffset)).isEqualTo(expected.get(y * srcWidth + x));
                    }
                }
            }
        }
    }

    @Test
    void testPartialBlockCropExtra() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc4u-part.dds");
        ByteBuffer expected = BCTestUtils.readPng("/bc4u-part.png");

        int srcWidth = 157;
        int srcHeight = 119;
        int dstOffset = 31;

        ByteBuffer dst = ByteBuffer.allocate(8 * 8 + dstOffset);
        BlockDecoder decoder = BlockDecoder.bc4(false);

        // Test all offsets between 0 and 8
        for (int srcY = 1; srcY < 8; srcY++) {
            for (int srcX = 1; srcX < 8; srcX++) {
                int w = 8;
                int h = 8;
                decoder.decode(
                    src.position(BCTestUtils.DDS_HEADER_SIZE), srcX, srcY, srcWidth, srcHeight,
                    dst.position(dstOffset), 0, 0, w, h,
                    w, h
                );

                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        assertThat(dst.get(y * w + x + dstOffset)).isEqualTo(expected.get((srcY + y) * srcWidth + (srcX + x)));
                    }
                }
            }
        }
    }

    @Test
    void testValidation() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.bc1(false)
                .decode(null, 0, 256))
            .withMessage("src width (0) or height (256) is not positive");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.bc1(false)
                .decode(null, 256, 0))
            .withMessage("src width (256) or height (0) is not positive");
    }

}
