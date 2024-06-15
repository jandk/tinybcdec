package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC1DecoderTest {

    private final BCDecoder decoder = new BC1Decoder(4, 3, 2, 1, 0, false);

    @Test
    void testBC1() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc1a.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc1a.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC1NoAlpha() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc1.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc1.png");

        assertThat(actual).isEqualTo(expected);
    }

}
