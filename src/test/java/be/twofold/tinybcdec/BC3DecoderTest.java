package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

class BC3DecoderTest {

    private final BC3Decoder decoder = new BC3Decoder();

    @Test
    void testBC3() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc3.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc3.png");

        BCTestUtils.compareBC(actual, expected);
    }

}
