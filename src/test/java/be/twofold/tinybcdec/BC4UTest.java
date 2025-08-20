package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC4UTest {

    private final BlockDecoder decoder = BlockDecoder.bc4(BlockDecoder.Signedness.UNSIGNED);

    @Test
    void testBC4U() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4u.dds");

        byte[] actual = decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readPng("/bc4u.png");

        assertThat(actual).isEqualTo(expected);
    }

}
