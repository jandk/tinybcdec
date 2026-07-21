package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

class BC4UFloatTest {

    private final BlockDecoder decoder = BlockDecoder.bc4Float(false);

    @Test
    void testBC4U() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc4u.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc4u.png", true);

        BCTestUtils.assertBufferEqualsFloats(actual, expected);
    }

}
