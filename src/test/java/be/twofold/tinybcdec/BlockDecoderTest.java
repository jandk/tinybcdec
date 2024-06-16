package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BlockDecoderTest {

    @Test
    void testPartialBlock() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC4Unsigned, PixelOrder.R)
            .decode(157, 119, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc4u-part.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testPartialBlockWithAlphaFill() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC4Unsigned, PixelOrder.RGBA)
            .decode(157, 119, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc4u-part.png");

        byte[] expectedWithAlpha = new byte[4 * expected.length];
        for (int i = 0, o = 0; i < expected.length; i++, o += 4) {
            expectedWithAlpha[o] = expected[i];
            expectedWithAlpha[o + 3] = (byte) 0xFF;
        }

        assertThat(actual).isEqualTo(expectedWithAlpha);
    }

    @Test
    void testAlphaFill() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC5Unsigned, PixelOrder.BGRA)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);

        assertThat(actual).satisfies(data -> {
            for (int i = 3; i < data.length; i += 4) {
                assertThat(data[i]).isEqualTo((byte) 0xFF);
            }
        });
    }

    @Test
    void testValidation() {
        assertThatNullPointerException()
            .isThrownBy(() -> BlockDecoder.create(null, PixelOrder.BGR));
        assertThatNullPointerException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1, null));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1, PixelOrder.R))
            .withMessage("greenChannel must be set for at least 2 bytes per pixel");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1, PixelOrder.of(2, 0, 1, -1, -1)))
            .withMessage("blueChannel must be set for at least 3 bytes per pixel");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1, PixelOrder.BGR))
            .withMessage("alphaChannel must be set for at least 4 bytes per pixel");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1, PixelOrder.RGBA)
                .decode(0, 256, null, 0))
            .withMessage("width must be greater than 0");
        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1, PixelOrder.RGBA)
                .decode(256, 0, null, 0))
            .withMessage("height must be greater than 0");
    }
}