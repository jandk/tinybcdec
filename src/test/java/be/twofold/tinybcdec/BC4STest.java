package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC4STest {

    private final BlockDecoder decoder = BlockDecoder.bc4(BlockDecoder.Signedness.SIGNED);

    @Test
    void testBC4S() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc4s.dds");

        byte[] actual = decoder.decode(src, BCTestUtils.DDS_HEADER_SIZE, 256, 256);
        byte[] expected = BCTestUtils.readPng("/bc4s.png");

        assertThat(actual).isEqualTo(expected);
    }

}
