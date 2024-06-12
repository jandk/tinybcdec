package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

class BC2DecoderTest {

    private final BCDecoder decoder = new BCDecoder(BCFormat.BC2, 4, 0, 1, 2, 3);

    @Test
    void testBC2() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc2.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc2.png");

        BCTestUtils.compareBC(actual, expected);
    }

}
