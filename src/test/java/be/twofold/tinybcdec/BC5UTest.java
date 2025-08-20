package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5UTest {

    @Test
    void testBC5U() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.bc5(BlockDecoder.Signedness.UNSIGNED)
            .decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readPng("/bc5u.png");

        for (int i = 0, o = 0; i < expected.length; i += 3, o += 2) {
            assertThat(actual[o/**/]).isEqualTo(expected[i/**/]);
            assertThat(actual[o + 1]).isEqualTo(expected[i + 1]);
        }
    }

}
