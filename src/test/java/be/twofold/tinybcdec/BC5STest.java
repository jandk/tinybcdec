package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BC5STest {

    @Test
    void testBC5S() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc5s.dds");

        ByteBuffer actual = BlockDecoder.bc5(true)
            .decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc5s.png");

        for (int i = 0; i < expected.remaining(); i += 4) {
            assertThat(Math.abs((actual.get(i + 1) & 0xFF) - (expected.get(i + 1) & 0xFF))).isLessThanOrEqualTo(1);
            assertThat(Math.abs((actual.get(i + 2) & 0xFF) - (expected.get(i + 2) & 0xFF))).isLessThanOrEqualTo(1);
        }
    }

}
