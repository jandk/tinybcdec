package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

class BC2DecoderTest {

    private final BC2Decoder decoder = new BC2Decoder();

    @Test
    void testBC2() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc2.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc2.png");

        BCTestUtils.compareBC(actual, expected);
    }

}