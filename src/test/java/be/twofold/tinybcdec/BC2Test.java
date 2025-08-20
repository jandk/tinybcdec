package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC2Test {

    private final BlockDecoder decoder = BlockDecoder.bc2();

    @Test
    void testBC2() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc2.dds");

        byte[] actual = decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readPng("/bc2.png");

        assertThat(actual).isEqualTo(expected);
    }

}
