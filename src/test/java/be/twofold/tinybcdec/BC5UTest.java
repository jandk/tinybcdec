package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5UTest {

    @Test
    void testBC5U() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5u.dds");

        byte[] actual = BlockDecoder.bc5(Signedness.UNSIGNED)
            .decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc5u.png");

        for (int i = 0, o = 0; i < expected.length; i += 3, o += 2) {
            assertThat(actual[o/**/]).isEqualTo(expected[i/**/]);
            assertThat(actual[o + 1]).isEqualTo(expected[i + 1]);
        }
    }

}
