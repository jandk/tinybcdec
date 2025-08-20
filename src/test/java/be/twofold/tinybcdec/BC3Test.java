package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC3Test {

    private final BlockDecoder decoder = BlockDecoder.bc3();

    @Test
    void testBC3() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc3.dds");

        byte[] actual = decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readPng("/bc3.png");

        assertThat(actual).isEqualTo(expected);
    }

}
