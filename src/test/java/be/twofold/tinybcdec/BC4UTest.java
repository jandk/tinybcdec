package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC4UTest {

    private final BlockDecoder decoder = BlockDecoder.bc4(false);

    @Test
    void testBC4U() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc4u.png");

        assertThat(actual).isEqualTo(expected);
    }

}
