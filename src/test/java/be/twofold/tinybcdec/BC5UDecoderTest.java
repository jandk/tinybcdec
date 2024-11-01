package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5UDecoderTest {

    @Test
    void testBC5U() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC5Unsigned)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5u.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC5UNormalized() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.create(BlockFormat.BC5UnsignedNormalized)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5u_normalized.png");

        for (int i = 0; i < expected.length; i += 4) {
            assertThat(actual[i + 0] & 0xFF).isEqualTo(expected[i + 0] & 0xFF);
            assertThat(actual[i + 1] & 0xFF).isEqualTo(expected[i + 1] & 0xFF);
            assertThat(actual[i + 3]).isEqualTo((byte) 0xFF);
            int za = actual[i + 2] & 0xFF;
            int ze = expected[i + 2] & 0xFF;
            if (ze != 0) {
                // texconv sets the channel to 0 outside of range, while I clamp, so I need to do the same
                assertThat(za).isEqualTo(ze);
            }
        }
    }

}
