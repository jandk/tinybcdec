package be.twofold.tinybcdec;

import org.junit.jupiter.api.*;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class BC7DecoderTest {

    private final BlockDecoder decoder = BlockDecoder.create(BlockFormat.BC7);

    @Test
    void testBC7() throws IOException {
        byte[] src = BCTestUtils.readResource("/bc7.dds");

        byte[] actual = decoder.decode(256, 256, src, BCTestUtils.DDS_HEADER_SIZE);
        byte[] expected = BCTestUtils.readPng("/bc7.png");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testBC7InvalidBlock() {
        byte[] src = new byte[16];
        byte[] actual = decoder.decode(4, 4, src, 0);
        assertThat(actual).isEqualTo(new byte[16 * 4]);
    }

}
