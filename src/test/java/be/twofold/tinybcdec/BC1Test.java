package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC1Test {

    private final BlockDecoder decoder = BlockDecoder.bc1(BlockDecoder.Opacity.TRANSPARENT);

    @Test
    void testBC1() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc1a.dds");

        byte[] actual = decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readPng("/bc1a.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC1NoAlpha() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc1.dds");

        byte[] actual = decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readPng("/bc1.png");

        assertThat(actual).isEqualTo(expected);
    }

}
