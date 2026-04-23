package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

import static org.assertj.core.api.Assertions.*;

class BC2Test {

    private final BlockDecoder decoder = BlockDecoder.bc2();

    @Test
    void testBC2() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc2.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc2.png");

        assertThat(actual).isEqualTo(expected);
    }

}
