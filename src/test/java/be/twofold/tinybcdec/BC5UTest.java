package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5UTest {

    @Test
    void testBC5U() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.bc5(false, false)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5u.png");

        for (int i = 0; i < expected.length; i += 3) {
            assertThat(actual[i/**/]).isEqualTo(expected[i/**/]);
            assertThat(actual[i + 1]).isEqualTo(expected[i + 1]);
            assertThat(actual[i + 2]).isZero();
        }
    }

    @Test
    void testBC5UReconstructZ() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.bc5(false, true)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5u_reconstructed.png");

        for (int i = 0; i < expected.length; i += 3) {
            assertThat(actual[i/**/]).isEqualTo(expected[i/**/]);
            assertThat(actual[i + 1]).isEqualTo(expected[i + 1]);
            if (expected[i + 2] != 0) {
                // texconv sets the channel to 0 outside of range, while I clamp, so I need to do the same
                assertThat(actual[i + 2]).isEqualTo(expected[i + 2]);
            }
        }
    }

}
