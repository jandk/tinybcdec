package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

class BC5UFloatTest {

    @Test
    void testBC5U() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc5u.dds");

        ByteBuffer actual = BlockDecoder.bc5Float(false)
            .decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc5u.png", true);

        BCTestUtils.assertBufferEqualsFloats(actual, expected);
    }

}
