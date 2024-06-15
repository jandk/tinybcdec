package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC7DecoderTest {

    private final BlockDecoder decoder = new BC7Decoder(4, 0, 1, 2, 3);

    @Test
    void testBC7() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc7.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc7.png");

        assertThat(actual).isEqualTo(expected);
    }

}
