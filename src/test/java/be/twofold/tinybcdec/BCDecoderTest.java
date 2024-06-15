package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BCDecoderTest {

    @Test
    void testPartialBlock() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");

        byte[] actual = BCDecoder.create(BCFormat.BC4Unsigned, BCOrder.R)
            .decode(157, 119, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc4u-part.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testPartialBlockWithAlphaFill() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");

        byte[] actual = BCDecoder.create(BCFormat.BC4Unsigned, BCOrder.RGBA)
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

        byte[] actual = BCDecoder.create(BCFormat.BC5Unsigned, BCOrder.BGRA)
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
            .isThrownBy(() -> BCDecoder.create(null, BCOrder.BGR));
        assertThatNullPointerException()
            .isThrownBy(() -> BCDecoder.create(BCFormat.BC1, null));

        assertThatThrownBy(() -> BCDecoder.create(BCFormat.BC1, BCOrder.BGR))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("alphaChannel must be set for at least 4 bytes per pixel");
    }
}