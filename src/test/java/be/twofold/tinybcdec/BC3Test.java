package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

class BC3Test {

    private final BlockDecoder decoder = BlockDecoder.bc3();

    @Test
    void testBC3() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc3.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc3.png");

        BCTestUtils.assertBufferEquals(actual, expected);
    }

}
