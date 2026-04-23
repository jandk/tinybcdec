package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BC4UTest {

    private final BlockDecoder decoder = BlockDecoder.bc4(false);

    @Test
    void testBC4U() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc4u.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc4u.png");

        assertThat(actual).isEqualTo(expected);
    }

}
