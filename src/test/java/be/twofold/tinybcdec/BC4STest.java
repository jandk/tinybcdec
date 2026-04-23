package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BC4STest {

    private final BlockDecoder decoder = BlockDecoder.bc4(true);

    @Test
    void testBC4S() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc4s.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc4s.png");

        assertThat(actual).isEqualTo(expected);
    }

}
