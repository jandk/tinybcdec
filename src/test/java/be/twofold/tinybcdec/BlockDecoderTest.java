package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BlockDecoderTest {

    @Test
    void testPartialBlock() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u-part.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC4Unsigned)
            .decode(157, 119, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc4u-part.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testAlphaFill() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC5Unsigned)
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
            .isThrownBy(() -> BlockDecoder.create(null));

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1)
                .decode(0, 256, null, 0))
            .withMessage("width must be greater than 0");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> BlockDecoder.create(BlockFormat.BC1)
                .decode(256, 0, null, 0))
            .withMessage("height must be greater than 0");
    }
}
