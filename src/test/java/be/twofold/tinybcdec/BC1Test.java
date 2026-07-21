package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.*;

class BC1Test {

    private final BlockDecoder decoder = BlockDecoder.bc1(false);

    @Test
    void testBC1() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc1a.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc1a.png");

        BCTestUtils.assertBufferEquals(actual, expected);
    }

    @Test
    void testBC1NoAlpha() throws IOException {
        ByteBuffer src = BCTestUtils.readResource("/bc1.dds");

        ByteBuffer actual = decoder.decode(src.position(BCTestUtils.DDS_HEADER_SIZE), 256, 256);
        ByteBuffer expected = BCTestUtils.readPng("/bc1.png");

        BCTestUtils.assertBufferEquals(actual, expected);
    }

}
