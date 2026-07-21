package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

class BC2FloatTest {

    private final BlockDecoder decoder = BlockDecoder.bc2Float();

    @Test
    void testBC2() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc2.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc2.png", true);

        BCTestUtils.assertBufferEqualsFloats(actual, expected);
    }

}
