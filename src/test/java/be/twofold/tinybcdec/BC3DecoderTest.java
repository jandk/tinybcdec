package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

class BC3DecoderTest {

    private final BCDecoder decoder = new BCDecoder(BCFormat.BC3, 4, 0, 1, 2, 3);

    @Test
    void testBC3() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc3.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc3.png");

        BCTestUtils.compareBC(actual, expected);
    }

}
