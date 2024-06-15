package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

class BC1DecoderTest {

    private final BlockDecoder decoder = new BC1Decoder(4, 3, 2, 1, 0, false);

    @Test
    void testBC1() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc1a.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc1a.png");

        BCTestUtils.compareBC(actual, expected, 3, 2, 1, 0);
    }

    @Test
    void testBC1NoAlpha() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc1.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc1.png");

        BCTestUtils.compareBC(actual, expected, 3, 2, 1, 0);
    }

}
