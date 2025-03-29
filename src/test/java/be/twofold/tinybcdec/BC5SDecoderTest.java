package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5SDecoderTest {

    @Test
    void testBC5S() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5s.dds");
        byte[] actual = BlockDecoder.create(BlockFormat.BC5S)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5s.png");

        for (int i = 0; i < expected.length; i += 3) {
            assertThat(Math.abs((actual[i + 0] & 0xFF) - (expected[i + 0] & 0xFF))).isLessThanOrEqualTo(1);
            assertThat(Math.abs((actual[i + 1] & 0xFF) - (expected[i + 1] & 0xFF))).isLessThanOrEqualTo(1);
            assertThat(actual[i + 2]).isZero();
        }
    }

    @Test
    void testBC5SNormalized() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5s.dds");
        byte[] actual = BlockDecoder.create(BlockFormat.BC5S_RECONSTRUCT_Z)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);

        byte[] expected = BCTestUtils.readPng("/bc5s_normalized.png");

        for (int i = 0; i < expected.length; i += 3) {
            assertThat(Math.abs((actual[i + 0] & 0xFF) - (expected[i + 0] & 0xFF))).isLessThanOrEqualTo(1);
            assertThat(Math.abs((actual[i + 1] & 0xFF) - (expected[i + 1] & 0xFF))).isLessThanOrEqualTo(1);
            if (expected[i + 2] != 0) {
                // texconv sets the channel to 0 outside of range, while I clamp, so I need to do the same
                assertThat(Math.abs((actual[i + 2] & 0xFF) - (expected[i + 2] & 0xFF))).isLessThanOrEqualTo(1);
            }
        }
    }

}
