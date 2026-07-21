package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

class BC5SFloatTest {

    @Test
    void testBC5U() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc5s.dds");

        ByteBuffer actual = BlockDecoder.bc5Float(true)
            .decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc5s.png", true);

        BCTestUtils.assertBufferEqualsFloats(actual, expected);
    }

}
