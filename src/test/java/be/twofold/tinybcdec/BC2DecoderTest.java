package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC2DecoderTest {

    private final BlockDecoder decoder = new BC2Decoder(4, 3, 2, 1, 0);

    @Test
    void testBC2() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc2.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc2.png");

        assertThat(actual).isEqualTo(expected);
    }

}
