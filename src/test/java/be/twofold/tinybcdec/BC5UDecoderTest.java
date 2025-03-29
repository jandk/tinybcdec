package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5UDecoderTest {

    @Test
    void testBC5U() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC5U)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5u.png");

        for (int i = 0; i < expected.length; i += 3) {
            assertThat(actual[i + 0]).isEqualTo(expected[i + 0]);
            assertThat(actual[i + 1]).isEqualTo(expected[i + 1]);
            assertThat(actual[i + 2]).isZero();
        }
    }

    @Test
    void testBC5UNormalized() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC5U_RECONSTRUCT_Z)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5u_normalized.png");

        for (int i = 0; i < expected.length; i += 3) {
            assertThat(actual[i + 0]).isEqualTo(expected[i + 0]);
            assertThat(actual[i + 1]).isEqualTo(expected[i + 1]);
            if (expected[i + 2] != 0) {
                // texconv sets the channel to 0 outside of range, while I clamp, so I need to do the same
                assertThat(actual[i + 2]).isEqualTo(expected[i + 2]);
            }
        }
    }

}
