package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BC5UTest {

    @Test
    void testBC5U() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc5u.dds");

        ByteBuffer actual = BlockDecoder.bc5(false)
            .decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc5u.png");

        for (int i = 0; i < expected.remaining(); i += 4) {
            assertThat(actual.get(i + 1)).isEqualTo(expected.get(i + 1));
            assertThat(actual.get(i + 2)).isEqualTo(expected.get(i + 2));
        }
    }

}
