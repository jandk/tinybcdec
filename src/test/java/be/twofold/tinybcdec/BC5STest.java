package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC5STest {

    @Test
    void testBC5S() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc5s.dds");
        byte[] actual = BlockDecoder.bc5(true)
            .decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readPng("/bc5s.png");

        for (int i = 0, o = 0; i < expected.length; i += 3, o += 2) {
            assertThat(Math.abs((actual[o/**/] & 0xFF) - (expected[i/**/] & 0xFF))).isLessThanOrEqualTo(1);
            assertThat(Math.abs((actual[o + 1] & 0xFF) - (expected[i + 1] & 0xFF))).isLessThanOrEqualTo(1);
        }
    }

}
